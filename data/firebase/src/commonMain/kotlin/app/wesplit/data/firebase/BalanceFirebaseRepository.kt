package app.wesplit.data.firebase

import app.wesplit.domain.balance.CalculateBalanceUsecase
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.expense.ExpenseRepository
import app.wesplit.domain.model.group.GroupRepository
import app.wesplit.domain.model.group.balance.Balance
import app.wesplit.domain.model.group.balance.BalanceRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single

@Single
class BalanceFirebaseRepository(
    private val expenseRepository: ExpenseRepository,
    private val groupRepository: GroupRepository,
    private val calculateBalanceUsecase: CalculateBalanceUsecase,
    private val analyticsManager: AnalyticsManager,
    private val coroutineDispatcher: CoroutineDispatcher,
) : BalanceRepository {
    // TODO: For now we support only 1 currency per group without possibility for FX
    // TODO: Cover by test
    override fun getByGroupId(groupId: String): Flow<Result<Balance>> =
        groupRepository.get(groupId).combine(expenseRepository.getByGroupId(groupId)) { groupResult, expenseResult ->
            if (groupResult.isFailure || expenseResult.isFailure) {
                Result.failure(groupResult.exceptionOrNull() ?: expenseResult.exceptionOrNull() ?: RuntimeException("Unkown exception"))
            } else {
                withContext(coroutineDispatcher) {
                    Result.success(
                        calculateBalanceUsecase(
                            group = groupResult.getOrThrow(),
                            expenses = expenseResult.getOrThrow(),
                        ),
                    )
                }
            }
        }
}
