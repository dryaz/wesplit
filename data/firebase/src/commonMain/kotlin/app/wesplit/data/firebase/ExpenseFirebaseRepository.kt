package app.wesplit.data.firebase

import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.expense.Expense
import app.wesplit.domain.model.expense.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

@Single
class ExpenseFirebaseRepository(
    private val analyticsManager: AnalyticsManager,
) : ExpenseRepository {
    private val expenses = MutableStateFlow<List<Expense>>(emptyList())

    // TODO: Check if firebase get local balances or maybe need to cache expenses by group id in order
    //  not to fetch this multiple times, e.g. for showing trxs and for computing balances.
    override fun getByGroupId(groupId: String): Flow<List<Expense>> = expenses.map { it.sortedByDescending { it.date } }

    override fun addExpense(
        groupId: String,
        expense: Expense,
    ) {
        expenses.update { it + expense }
    }
}
