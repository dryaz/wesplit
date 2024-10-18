package app.wesplit.domain.balance

import app.wesplit.domain.model.currency.Amount
import app.wesplit.domain.model.currency.FxRates
import app.wesplit.domain.model.group.Balance
import org.koin.core.annotation.Single

@Single
class BalanceCalculationUseCase {
    fun recalculate(
        balance: Balance,
        fxRates: FxRates,
        selectedCurrency: String,
    ): Balance {
        return balance.copy(
            participantsBalance =
                balance.participantsBalance.map {
                    it.copy(
                        amounts =
                            it.amounts.recalculateAmounts(
                                fxRates = fxRates,
                                targetCurrency = selectedCurrency,
                            ).toSet(),
                    )
                }.toSet(),
            undistributed =
                balance.undistributed.recalculateAmounts(
                    fxRates = fxRates,
                    targetCurrency = selectedCurrency,
                ).toSet(),
        )
    }

    private inline fun <reified C : Collection<Amount>> C.recalculateAmounts(
        fxRates: FxRates,
        targetCurrency: String,
    ): List<Amount> {
        val result = mutableMapOf<String, Double>()

        forEach { amount ->
            // Correctly determine sourceRate based on the base currency
            val sourceRate = if (amount.currencyCode == fxRates.base) 1.0 else fxRates.rates[amount.currencyCode]

            // Correctly determine targetRate based on the base currency
            val targetRate = if (targetCurrency == fxRates.base) 1.0 else fxRates.rates[targetCurrency]

            if (sourceRate != null && targetRate != null) {
                // Convert amount to targetCurrency
                val convertedValue = amount.value * (targetRate / sourceRate)
                // Aggregate the converted value under targetCurrency
                result[targetCurrency] = (result[targetCurrency] ?: 0.0) + convertedValue
            } else {
                // If conversion isn't possible, retain the original currency and sum the values
                result[amount.currencyCode] = (result[amount.currencyCode] ?: 0.0) + amount.value
            }
        }

        return result.map { Amount(currencyCode = it.key, value = it.value) }
    }
}
