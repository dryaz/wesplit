package app.wesplit.domain.settle

import app.wesplit.domain.model.currency.Amount
import app.wesplit.domain.model.group.Balance
import app.wesplit.domain.model.group.Participant
import org.koin.core.annotation.Single
import kotlin.math.abs

@Single
class SettleSuggestionUseCase {
    fun get(balance: Balance): List<SettleSuggestion> {
        val suggestions = mutableListOf<SettleSuggestion>()
        val creditors = mutableListOf<Pair<Participant, Amount>>()
        val debitors = mutableListOf<Pair<Participant, Amount>>()

        balance.participantsBalance.forEach { balance ->
            balance.amounts.forEach { amount ->
                if (amount.value > 0) creditors.add(balance.participant to amount)
                if (amount.value < 0) debitors.add(balance.participant to amount)
            }
        }

        debitors.sortBy { it.second.value }
        creditors.sortByDescending { it.second.value }

        // Settle equal sums if any
        var i = debitors.size - 1
        while (i >= 0) {
            var j = creditors.size - 1
            while (j >= 0) {
                if (debitors[i].second.currencyCode == creditors[j].second.currencyCode &&
                    abs(debitors[i].second.value) == abs(creditors[j].second.value)
                ) {
                    // Add a settle suggestion for the matching amounts
                    suggestions.add(
                        SettleSuggestion(
                            payer = debitors[i].first,
                            recipient = creditors[j].first,
                            amount = creditors[i].second,
                        ),
                    )

                    // Remove the settled debtor and creditor
                    debitors.removeAt(i)
                    creditors.removeAt(j)

                    // Since we removed the current debtor, no need to increment `i`
                    break // Exit the inner loop and continue with the next debtor
                } else {
                    j--
                }
            }
            i--
        }

        i = debitors.size - 1 // Index for debtors

        // Iterate over remaining debtors
        while (i >= 0) {
            val (debtor, debtAmount) = debitors[i]

            // Find a matching creditor with the same currency
            var j = creditors.size - 1
            while (j >= 0) {
                val (creditor, creditAmount) = creditors[j]

                // Only settle if the currencies match
                if (debtAmount.currencyCode == creditAmount.currencyCode) {
                    // Determine the amount to settle
                    val settlementAmount = minOf(-debtAmount.value, creditAmount.value)

                    // Add a suggestion
                    suggestions.add(
                        SettleSuggestion(
                            payer = debtor,
                            recipient = creditor,
                            amount = Amount(settlementAmount, debtAmount.currencyCode),
                        ),
                    )

                    // Update the balances
                    debitors[i] = debtor to Amount(debtAmount.value + settlementAmount, debtAmount.currencyCode)
                    creditors[j] = creditor to Amount(creditAmount.value - settlementAmount, creditAmount.currencyCode)

                    // If creditor is fully settled, remove it
                    if (creditors[j].second.value == 0.0) {
                        creditors.removeAt(j)
                    }

                    // If debtor is fully settled, break out of the inner loop
                    if (debitors[i].second.value == 0.0) {
                        debitors.removeAt(i)
                        break
                    }
                }
                j--
            }
            i--
        }

        creditors.forEach {
            suggestions.add(SettleSuggestion(payer = null, recipient = it.first, amount = it.second))
        }

        return suggestions.filterNot { it.amount.value < 0.01 }
    }
}

data class SettleSuggestion(
    val payer: Participant?,
    val recipient: Participant,
    val amount: Amount,
)
