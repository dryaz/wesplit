package app.wesplit.data.firebase

import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.expense.Expense
import app.wesplit.domain.model.expense.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single
import kotlin.random.Random

@Single
class ExpenseFirebaseRepository(
    private val analyticsManager: AnalyticsManager,
) : ExpenseRepository {
    private val expenses = MutableStateFlow<List<Expense>>(emptyList())

    // TODO: Check if firebase get local balances or maybe need to cache expenses by group id in order
    //  not to fetch this multiple times, e.g. for showing trxs and for computing balances.
    override fun getByGroupId(groupId: String): Flow<Result<List<Expense>>> =
        expenses.map { Result.success(it.sortedByDescending { it.date }) }

    override fun getById(expenseId: String): Flow<Result<Expense>> =
        flow {
            val expense = expenses.value.find { it.id != null && it.id == expenseId }
            if (expense != null) {
                emit(Result.success(expense))
            } else {
                emit(Result.failure(NullPointerException("Expense with id $expenseId not exist")))
            }
        }

    override fun addExpense(
        groupId: String,
        expense: Expense,
    ) {
        expenses.update { it + expense.copy(id = Random.nextInt().toString()) }
    }
}
