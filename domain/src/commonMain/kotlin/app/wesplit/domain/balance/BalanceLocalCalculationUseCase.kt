package app.wesplit.domain.balance

import app.wesplit.domain.model.currency.Amount
import app.wesplit.domain.model.expense.Expense
import app.wesplit.domain.model.group.Balance
import app.wesplit.domain.model.group.ParticipantBalance
import org.koin.core.annotation.Single

@Single
class BalanceLocalCalculationUseCase {
    operator fun invoke(expenses: List<Expense>): Balance {
        val participantBalance = mutableMapOf<String, MutableMap<String, Double>>()
        var nonDistributed = mutableMapOf<String, Double>()

        expenses.forEach { expense ->
            val currency = expense.totalAmount.currencyCode
            expense.shares.forEach { share ->
                val currentBalance = participantBalance.get(share.participant.id)?.get(currency) ?: 0.0
                val currentPayerBalance = participantBalance.get(expense.payedBy.id)?.get(currency) ?: 0.0
                if (share.participant.id != expense.payedBy.id) {
                    if (!participantBalance.containsKey(share.participant.id)) {
                        participantBalance[share.participant.id] = mutableMapOf()
                    }
                    if (!participantBalance.containsKey(expense.payedBy.id)) {
                        participantBalance[expense.payedBy.id] = mutableMapOf()
                    }
                    participantBalance[share.participant.id]!![currency] = currentBalance - share.amount.value
                    participantBalance[expense.payedBy.id]!![currency] = currentPayerBalance + share.amount.value
                }
            }
            var residual = expense.undistributedAmount?.value ?: 0.0
            if (residual != 0.0) {
                if (!participantBalance.containsKey(expense.payedBy.id)) {
                    participantBalance[expense.payedBy.id] = mutableMapOf()
                }
                participantBalance[expense.payedBy.id]!![currency] = (participantBalance[expense.payedBy.id]!![currency] ?: 0.0) + residual
                nonDistributed[currency] = (nonDistributed[currency] ?: 0.0) + residual
            }
        }

        return Balance(
            participantsBalance =
                participantBalance.mapNotNull { calculated ->
                    ParticipantBalance(
                        participant = expenses.firstNotNullOf { it.shares.firstOrNull { it.participant.id == calculated.key } }.participant,
                        amounts =
                            calculated.value.mapNotNull {
                                if (it.value == 0.0) {
                                    null
                                } else {
                                    Amount(
                                        value = it.value,
                                        currencyCode = it.key,
                                    )
                                }
                            }.toSet(),
                    )
                }.toSet(),
            undistributed =
                nonDistributed.mapNotNull { calculated ->
                    if (calculated.value == 0.0) {
                        null
                    } else {
                        Amount(
                            value = calculated.value,
                            currencyCode = calculated.key,
                        )
                    }
                }.toSet(),
            invalid = false,
        )
    }
}
