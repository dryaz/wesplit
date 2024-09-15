package app.wesplit.domain.balance

import app.wesplit.domain.model.expense.Amount
import app.wesplit.domain.model.expense.Expense
import app.wesplit.domain.model.group.Group
import app.wesplit.domain.model.group.balance.Balance
import app.wesplit.domain.model.group.balance.ParticipantStat
import org.koin.core.annotation.Single

@Single
class CalculateBalanceUsecase {
    operator fun invoke(
        group: Group,
        expenses: List<Expense>,
    ): Balance {
        val participantBalance = group.participants.associateWith { 0f }.toMutableMap()

        var nonDistributed = 0f

        expenses.forEach { expense ->
            expense.shares.forEach { share ->
                val currentBalance = participantBalance.get(share.participant) ?: 0f
                val currentPayerBalance = participantBalance.get(expense.payedBy) ?: 0f
                if (share.participant != expense.payedBy) {
                    participantBalance[share.participant] = currentBalance - share.amount.value
                    participantBalance[expense.payedBy] = currentPayerBalance + share.amount.value
                }
            }
            val residual = expense.undistributedAmount?.value ?: 0f
            participantBalance[expense.payedBy] = (participantBalance[expense.payedBy] ?: 0f) + residual
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
}
