package app.wesplit.data.firebase

import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.LogLevel
import app.wesplit.domain.model.expense.Expense
import app.wesplit.domain.model.expense.ExpenseRepository
import app.wesplit.domain.model.expense.ExpenseStatus
import app.wesplit.domain.model.user.Setting
import app.wesplit.domain.model.user.UserRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.ServerTimestampBehavior
import dev.gitlive.firebase.firestore.WriteBatch
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single

private const val STATUS_FIELD = "status"

private const val EXPENSE_COLLECTION = "expenses"
private const val DATE_FIELD = "date"

private const val EXPENSE_CREATE_EVENT = "expense_create"
private const val EXPENSE_UPDATE_EVENT = "expense_update"
private const val EXPENSE_DELETE_EVENT = "expense_delete"
private const val EXPENSE_SPLIT_TYPE_PARAM = "split_type"

private const val SETTLED_COMPLETE = "settlment_completed"

@Single
class ExpenseFirebaseRepository(
    private val coroutineDispatcher: CoroutineDispatcher,
    private val userRepository: UserRepository,
    private val analyticsManager: AnalyticsManager,
) : ExpenseRepository {
    // TODO: Check if firebase get local balances or maybe need to cache expenses by group id in order
    //  not to fetch this multiple times, e.g. for showing trxs and for computing balances.
    // TODO: Pagination for expenses
    override fun getByGroupId(groupId: String): Flow<Result<List<Expense>>> =
        Firebase.firestore.collection(GROUP_COLLECTION)
            .document(groupId)
            .collection(EXPENSE_COLLECTION)
            .orderBy(DATE_FIELD, Direction.DESCENDING).snapshots.map {
                // TODO: Exception to failuer result. Group could be not existing, security rules could fail etc.
                // TODO: Sort by creation date directly on response
                withContext(coroutineDispatcher) {
                    val expenses =
                        it.documents.map {
                            it.data(Expense.serializer(), ServerTimestampBehavior.ESTIMATE).copy(
                                id = it.id,
                            )
                        }

                    Result.success(expenses)
                }
            }

    override fun getById(
        groupId: String,
        expenseId: String,
    ): Flow<Result<Expense>> =
        Firebase.firestore.collection(GROUP_COLLECTION).document(groupId).collection(EXPENSE_COLLECTION).document(expenseId).snapshots.map {
            if (it.exists) {
                val expense = it.data(Expense.serializer(), ServerTimestampBehavior.ESTIMATE)
                Result.success(expense.copy(id = it.id))
            } else {
                // TODO: Check what if there is not enough permission
                val exception = NullPointerException("No expense with id $expenseId found")
                analyticsManager.log(exception)
                Result.failure(exception)
            }
        }

    override suspend fun commit(
        groupId: String,
        expense: Expense,
    ): Unit =
        withContext(coroutineDispatcher + NonCancellable) {
            val newExpense = expense.copy(status = ExpenseStatus.NEW)
            userRepository.update(Setting.Currency(newExpense.totalAmount.currencyCode))
            val expenseId = newExpense.id
            val eventName = if (expenseId != null) EXPENSE_UPDATE_EVENT else EXPENSE_CREATE_EVENT

            analyticsManager.track(eventName)

            if (expenseId != null) {
                val doc =
                    Firebase.firestore.collection(
                        GROUP_COLLECTION,
                    ).document(groupId).collection(EXPENSE_COLLECTION).document(expenseId).get()
                if (doc.exists) {
                    Firebase.firestore.collection(
                        GROUP_COLLECTION,
                    ).document(groupId).collection(EXPENSE_COLLECTION).document(expenseId).update(
                        strategy = Expense.serializer(),
                        // TODO: Point of improvement - send only changed values.
                        data = newExpense,
                    )
                } else {
                    // TODO: Fire back and error to ui
                    analyticsManager.log(IllegalStateException("Try to edit expense document with id $expenseId which not exists"))
                }
            } else {
                Firebase.firestore.collection(GROUP_COLLECTION).document(groupId).collection(EXPENSE_COLLECTION).add(
                    strategy = Expense.serializer(),
                    data = newExpense,
                )
            }
        }

    override suspend fun delete(
        groupId: String,
        expense: Expense,
    ) {
        withContext(NonCancellable) {
            analyticsManager.track(EXPENSE_DELETE_EVENT)
            val expenseId = expense.id
            if (expenseId != null) {
                Firebase.firestore.collection(
                    GROUP_COLLECTION,
                ).document(groupId).collection(EXPENSE_COLLECTION).document(expenseId).delete()
            } else {
                analyticsManager.log("Try to delete expense with null ID", LogLevel.ERROR)
            }
        }
    }

    override suspend fun settle(groupId: String) {
        // TODO: Stale balances here as well
        withContext(coroutineDispatcher + NonCancellable) {
            val firestore = Firebase.firestore
            val expensesRef =
                firestore
                    .collection(GROUP_COLLECTION)
                    .document(groupId)
                    .collection(EXPENSE_COLLECTION)

            // Use the 'where' method with field, operator, and value
            val query =
                expensesRef.where {
                    (STATUS_FIELD equalTo null).or(STATUS_FIELD equalTo ExpenseStatus.NEW.name)
                }

            try {
                val querySnapshot = query.get()
                val documents = querySnapshot.documents

                if (documents.isEmpty()) {
                    analyticsManager.log("No expenses with status NEW to update", LogLevel.WARNING)
                    return@withContext
                }

                // Prepare batches
                val batches = mutableListOf<WriteBatch>()
                var batch = firestore.batch()
                var operationCount = 0

                for ((index, document) in documents.withIndex()) {
                    val expenseRef = document.reference
                    batch.update(expenseRef, "status" to ExpenseStatus.SETTLED.name)
                    operationCount++

                    // Firestore allows a maximum of 500 operations per batch
                    if (operationCount == 500 || index == documents.lastIndex) {
                        analyticsManager.log("max batch of 500 reached", LogLevel.WARNING)
                        batches.add(batch)
                        batch = firestore.batch()
                        operationCount = 0
                    }
                }

                for ((batchIndex, batchToCommit) in batches.withIndex()) {
                    batchToCommit.commit()
                    analyticsManager.log("Batch ${batchIndex + 1} committed successfully.", LogLevel.DEBUG)
                }

                analyticsManager.track(SETTLED_COMPLETE)
            } catch (e: Exception) {
                analyticsManager.log(e)
            }
        }
    }
}
