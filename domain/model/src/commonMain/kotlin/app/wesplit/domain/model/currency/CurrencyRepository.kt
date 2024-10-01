package app.wesplit.domain.model.currency

import kotlinx.coroutines.flow.StateFlow

interface CurrencyRepository {
    fun getFxRates(): StateFlow<FxRates>

    fun getAvailableCurrencyCodes(): StateFlow<CurrencyCodesCollection>
}

data class CurrencyCodesCollection(
    val lru: List<String>,
    val all: List<String>,
)
