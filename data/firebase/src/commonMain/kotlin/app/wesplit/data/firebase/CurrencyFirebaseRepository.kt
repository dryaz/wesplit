package app.wesplit.data.firebase

import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.currency.CurrencyCodesCollection
import app.wesplit.domain.model.currency.CurrencyRepository
import app.wesplit.domain.model.currency.FxRates
import app.wesplit.domain.model.currency.currencySymbols
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.ServerTimestampBehavior
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

private const val FX_COLLECTION = "fxrates"
private const val FX_LATEST = "latest"

@Single
class CurrencyFirebaseRepository(
    private val analyticsManager: AnalyticsManager,
    private val coroutineDispatcher: CoroutineDispatcher,
) : CurrencyRepository {
    private val currencyCollection =
        MutableStateFlow(
            CurrencyCodesCollection(
                lru = listOf("USD", "EUR", "GBP", "CHF", "CAD"),
                all = currencySymbols.keys.toList(),
            ),
        )

    private val fxRates =
        MutableStateFlow(
            FxRates(
                base = "USD",
                rates = currencySymbols.keys.associateWith { 0.0 },
                updatedAt = null,
            ),
        )

    init {
        CoroutineScope(coroutineDispatcher).launch {
            Firebase.firestore.collection(FX_COLLECTION).document(FX_LATEST).snapshots.map {
                if (it.exists) {
                    val rates = it.data(FxRates.serializer(), ServerTimestampBehavior.ESTIMATE)
                    fxRates.update { rates }
                } else {
                    val exception = NullPointerException("No fx rates found")
                    analyticsManager.log(exception)
                }
            }.retryWhen { cause, attempt ->
                if (attempt > 3) return@retryWhen false
                cause.printStackTrace()
                analyticsManager.log(cause)
                return@retryWhen true
            }
        }
    }

    override fun getFxRates(): StateFlow<FxRates> = fxRates

    override fun getAvailableCurrencyCodes(): StateFlow<CurrencyCodesCollection> = currencyCollection
}
