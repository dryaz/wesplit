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
     * @param groupId Gropu Id to get expense from. At the moment expenses attached to group.
     * @param expenseId Expense Id to get details for
     */
    fun getById(
        groupId: String,
        expenseId: String,
    ): Flow<Result<Expense>>

    // TODO: Return Result?
    suspend fun commit(
        groupId: String,
        expense: Expense,
    )

    suspend fun delete(
        groupId: String,
        expense: Expense,
    )
}
