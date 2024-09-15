package app.wesplit.data.firebase

import app.wesplit.domain.balance.CalculateBalanceUsecase
import app.wesplit.domain.model.expense.Amount
import app.wesplit.domain.model.expense.Expense
import app.wesplit.domain.model.expense.ExpenseRepository
import app.wesplit.domain.model.group.Group
import app.wesplit.domain.model.group.GroupRepository
import app.wesplit.domain.model.group.balance.Balance
import app.wesplit.domain.model.group.balance.BalanceRepository
import app.wesplit.domain.model.group.balance.ParticipantStat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.koin.core.annotation.Single

@Single
class BalanceFirebaseRepository(
    private val expenseRepository: ExpenseRepository,
    private val groupRepository: GroupRepository,
    private val calculateBalanceUsecase: CalculateBalanceUsecase,
) : BalanceRepository {
    // TODO: For now we support only 1 currency per group without possibility for FX
    // TODO: Cover by test
    override fun getByGroupId(groupId: String): Flow<Result<Balance>> =
        groupRepository.get(groupId).combine(expenseRepository.getByGroupId(groupId)) { groupResult, expenseResult ->
            if (groupResult.isFailure || expenseResult.isFailure) {
                Result.failure(groupResult.exceptionOrNull() ?: expenseResult.exceptionOrNull() ?: RuntimeException("Unkown exception"))
            } else {
                Result.success(
                    calculateBalanceUsecase(
                        group = groupResult.getOrThrow(),
                        expenses = expenseResult.getOrThrow(),
                    ),
                )
            }
        }
}

private fun calculateBalances(
    group: Group,
    expenses: List<Expense>,
): Balance {
    val participantBalance = group.participants.associateWith { 0f }.toMutableMap()

    var nonDistributed = 0f

    expenses.forEach { expense ->
        var residual = expense.totalAmount.value
        expense.shares.forEach { share ->
            val currentBalance = participantBalance.get(share.participant) ?: 0f
            val currentPayerBalance = participantBalance.get(expense.payedBy) ?: 0f
            if (share.participant != expense.payedBy) {
                participantBalance[share.participant] = currentBalance - share.amount.value
                participantBalance[expense.payedBy] = currentPayerBalance + share.amount.value
            }
            residual -= share.amount.value
        }
        nonDistributed += residual
    }

    val currencyCode = "USD" // expenses[0].totalAmount.currencyCode

    return Balance(
        participants =
            participantBalance.keys.map {
                it to
                    ParticipantStat(
                        balance = Amount(value = participantBalance[it] ?: 0f, currencyCode = currencyCode),
                    )
            }.toMap(),
        nonDistributed = Amount(nonDistributed, currencyCode),
    )
}
