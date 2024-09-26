package app.wesplit.data.firebase

import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.LogLevel
import app.wesplit.domain.model.expense.Expense
import app.wesplit.domain.model.expense.ExpenseRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.ServerTimestampBehavior
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single

private const val EXPENSE_COLLECTION = "expenses"
private const val DATE_FIELD = "date"

private const val EXPENSE_CREATE_EVENT = "expense_create"
private const val EXPENSE_UPDATE_EVENT = "expense_update"
private const val EXPENSE_DELETE_EVENT = "expense_delete"
private const val EXPENSE_SPLIT_TYPE_PARAM = "split_type"

@Single
class ExpenseFirebaseRepository(
    private val coroutineDispatcher: CoroutineDispatcher,
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
            val expenseId = expense.id
            val eventName = if (expenseId != null) EXPENSE_UPDATE_EVENT else EXPENSE_CREATE_EVENT

            analyticsManager.track(
                eventName,
                mapOf(
                    EXPENSE_SPLIT_TYPE_PARAM to expense.splitType.name,
                ),
            )

            if (expenseId != null) {
                val doc =
                    Firebase.firestore.collection(
                        GROUP_COLLECTION,
                    ).document(groupId).collection(EXPENSE_COLLECTION).document(expenseId).get()
                if (doc.exists) {
                    val existingExpense = doc.data(Expense.serializer(), ServerTimestampBehavior.ESTIMATE)
                    Firebase.firestore.collection(
                        GROUP_COLLECTION,
                    ).document(groupId).collection(EXPENSE_COLLECTION).document(expenseId).update(
                        strategy = Expense.serializer(),
                        // TODO: Point of improvement - send only changed values.
                        data = expense,
                    )
                } else {
                    // TODO: Fire back and error to ui
                    analyticsManager.log(IllegalStateException("Try to edit expense document with id $expenseId which not exists"))
                }
            } else {
                Firebase.firestore.collection(GROUP_COLLECTION).document(groupId).collection(EXPENSE_COLLECTION).add(
                    strategy = Expense.serializer(),
                    data = expense,
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
}
