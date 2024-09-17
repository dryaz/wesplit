package app.wesplit.domain.model.expense

import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    /**
     * Get expenses for the group.
     *
     * @param groupId id of the group to fetch expenses.
     */
    fun getByGroupId(groupId: String): Flow<Result<List<Expense>>>

    /**
     * Get particular expense by id.
     *
     * @param expenseId Expense Id to get details for
     */
    fun getById(expenseId: String): Flow<Result<Expense>>

    // TODO: Return Result?
    fun addExpense(
        groupId: String,
        expense: Expense,
    )
}
