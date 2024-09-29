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
        val participantBalance = group.participants.map { it.id }.associateWith { 0.0 }.toMutableMap()

        var nonDistributed = 0.0

        expenses.forEach { expense ->
            var residual = expense.totalAmount.value
            expense.shares.forEach { share ->
                val currentBalance = participantBalance.get(share.participant.id) ?: 0.0
                val currentPayerBalance = participantBalance.get(expense.payedBy.id) ?: 0.0
                if (share.participant.id != expense.payedBy.id) {
                    participantBalance[share.participant.id] = currentBalance - share.amount.value
                    participantBalance[expense.payedBy.id] = currentPayerBalance + share.amount.value
                }
                residual -= share.amount.value
            }
            participantBalance[expense.payedBy.id] = (participantBalance[expense.payedBy.id] ?: 0.0) + residual
            nonDistributed += residual
        }

        val currencyCode = "USD" // expenses[0].totalAmount.currencyCode

        return Balance(
            participants =
                group.participants.map {
                    it to
                        ParticipantStat(
                            balance = Amount(value = participantBalance[it.id] ?: 0.0, currencyCode = currencyCode),
                        )
                }.toMap(),
            nonDistributed = Amount(nonDistributed, currencyCode),
        )
    }
}
