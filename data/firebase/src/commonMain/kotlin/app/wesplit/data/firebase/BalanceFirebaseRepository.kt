package app.wesplit.data.firebase

import app.wesplit.domain.model.expense.Amount
import app.wesplit.domain.model.expense.ExpenseRepository
import app.wesplit.domain.model.group.Participant
import app.wesplit.domain.model.group.balance.Balance
import app.wesplit.domain.model.group.balance.BalanceRepository
import app.wesplit.domain.model.group.balance.ParticipantStat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class BalanceFirebaseRepository(
    private val expenseRepository: ExpenseRepository,
) : BalanceRepository {
    // TODO: For now we support only 1 currency per group without possibility for FX
    // TODO: Cover by test
    override fun getByGroupId(groupId: String): Flow<Balance?> =
        expenseRepository.getByGroupId(groupId).map { expenses ->
            if (expenses.isEmpty()) return@map null

            val participantBalance = mutableMapOf<Participant, Float>()
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

            val currencyCode = expenses[0].totalAmount.currencyCode

            Balance(
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
}
