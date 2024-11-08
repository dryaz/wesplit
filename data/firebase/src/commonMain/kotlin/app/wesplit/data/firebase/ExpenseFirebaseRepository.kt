package app.wesplit.data.firebase

import app.wesplit.Permission
import app.wesplit.PermissionsDelegate
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.LogLevel
import app.wesplit.domain.model.expense.Expense
import app.wesplit.domain.model.expense.ExpenseRepository
import app.wesplit.domain.model.expense.ExpenseStatus
import app.wesplit.domain.model.user.Setting
import app.wesplit.domain.model.user.UserRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.ServerTimestampBehavior
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.WriteBatch
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single

private const val STATUS_FIELD = "status"
private const val LAST_UPDATED_FIELD = "lastUpdatedAt"
private const val PROTECTION_FIELD = "protectedBy"

private const val EXPENSE_COLLECTION = "expenses"
private const val DATE_FIELD = "date"
private const val INVALID_BALANCE_FIELD = "balances.status"

private const val EXPENSE_CREATE_EVENT = "expense_create"
private const val EXPENSE_UPDATE_EVENT = "expense_update"
private const val EXPENSE_DELETE_EVENT = "expense_delete"
private const val EXPENSE_SPLIT_TYPE_PARAM = "split_type"

private const val PUSH_ENABLED = "push_enabled"

private const val SETTLED_COMPLETE = "settlment_completed"

@Single
class ExpenseFirebaseRepository(
    private val coroutineDispatcher: CoroutineDispatcher,
    private val userRepository: UserRepository,
    private val analyticsManager: AnalyticsManager,
    private val permissionsDelegate: PermissionsDelegate,
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
            val newExpense =
                expense.copy(
                    lastUpdated = Timestamp.ServerTimestamp,
                )
            userRepository.update(Setting.Currency(newExpense.totalAmount.currencyCode))
            val expenseId = newExpense.id
            val eventName = if (expenseId != null) EXPENSE_UPDATE_EVENT else EXPENSE_CREATE_EVENT

            analyticsManager.track(eventName)

            val batch = Firebase.firestore.batch()
            if (expenseId != null) {
                val doc =
                    Firebase.firestore.collection(
                        GROUP_COLLECTION,
                    ).document(groupId).collection(EXPENSE_COLLECTION).document(expenseId).get()
                if (doc.exists) {
                    batch.update(
                        documentRef =
                            Firebase.firestore.collection(
                                GROUP_COLLECTION,
                            ).document(groupId).collection(EXPENSE_COLLECTION).document(expenseId),
                        strategy = Expense.serializer(),
                        // TODO: Point of improvement - send only changed values.
                        data = newExpense,
                    )

                    if (expense.protectionList.isEmpty()) {
                        batch.update(
                            documentRef =
                                Firebase.firestore.collection(GROUP_COLLECTION)
                                    .document(groupId).collection(EXPENSE_COLLECTION).document(expenseId),
                            data = mapOf(PROTECTION_FIELD to FieldValue.delete),
                        )
                    }
                } else {
                    // TODO: Fire back and error to ui
                    analyticsManager.log(IllegalStateException("Try to edit expense document with id $expenseId which not exists"))
                    return@withContext
                }
            } else {
                val newExpenseRef =
                    Firebase.firestore.collection(GROUP_COLLECTION).document(groupId).collection(EXPENSE_COLLECTION).document
                batch.set(
                    documentRef = newExpenseRef,
                    strategy = Expense.serializer(),
                    data = newExpense,
                )
            }
            batch.update(
                documentRef = Firebase.firestore.collection(GROUP_COLLECTION).document(groupId),
                INVALID_BALANCE_FIELD to "invalid",
            )
            batch.commit()

            withContext(Dispatchers.Main) {
                val result = permissionsDelegate.requestPermission(Permission.PUSH)
                analyticsManager.setParam(PUSH_ENABLED, "${result.isSuccess}")
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
                val batch = Firebase.firestore.batch()
                batch.delete(
                    Firebase.firestore.collection(
                        GROUP_COLLECTION,
                    ).document(groupId).collection(EXPENSE_COLLECTION).document(expenseId),
                )
                batch.update(
                    documentRef = Firebase.firestore.collection(GROUP_COLLECTION).document(groupId),
                    INVALID_BALANCE_FIELD to "invalid",
                )
                batch.commit()
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
                    batch.update(
                        expenseRef,
                        STATUS_FIELD to ExpenseStatus.SETTLED.name,
                        LAST_UPDATED_FIELD to Timestamp.ServerTimestamp,
                    )
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
                    batchToCommit.update(
                        documentRef = Firebase.firestore.collection(GROUP_COLLECTION).document(groupId),
                        INVALID_BALANCE_FIELD to "invalid",
                    )
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
