package app.wesplit.domain.model.currency

import kotlinx.coroutines.flow.StateFlow

interface CurrencyRepository {
    fun getFxRates(): StateFlow<FxState>

    fun getAvailableCurrencyCodes(): StateFlow<CurrencyCodesCollection>
}

data class CurrencyCodesCollection(
    val lru: List<String>,
    val all: List<String>,
)

sealed interface FxState {
    data object Loading : FxState

    data class Error(val type: Type) : FxState {
        enum class Type {
            PLUS_NEEDED,
            FETCH_ERROR,
        }
    }

    data class Data(val fxRates: FxRates) : FxState
}
