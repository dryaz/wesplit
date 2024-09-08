package app.wesplit.domain.model.group.balance

import kotlinx.coroutines.flow.Flow

interface BalanceRepository {
    /**
     * Get participant's balance for the group.
     *
     * @param groupId id of the group to fetch expenses.
     */
    fun getByGroupId(groupId: String): Flow<Balance?>
}
