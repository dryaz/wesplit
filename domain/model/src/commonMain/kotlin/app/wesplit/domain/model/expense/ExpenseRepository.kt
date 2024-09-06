package app.wesplit.domain.model.expense

import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    /**
     * Get expenses for the group.
     *
     * @param groupId id of the group to fetch expenses.
     * @param cursorExpenseId id of the last fetched expense to support partial loading and pagination.
     */
    fun getByGroupId(
        groupId: String,
        cursorExpenseId: String,
    ): Flow<List<Expense>>
}
