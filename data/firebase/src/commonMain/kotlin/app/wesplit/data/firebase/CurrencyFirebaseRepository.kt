package app.wesplit.data.firebase

import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.currency.CurrencyCodesCollection
import app.wesplit.domain.model.currency.CurrencyRepository
import app.wesplit.domain.model.currency.FxRates
import app.wesplit.domain.model.currency.FxState
import app.wesplit.domain.model.currency.currencySymbols
import app.wesplit.domain.model.user.UserRepository
import app.wesplit.domain.model.user.isPlus
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.ServerTimestampBehavior
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.Single

private const val FX_COLLECTION = "fxrates"
private const val FX_LATEST = "latest"

@Single
class CurrencyFirebaseRepository(
    private val analyticsManager: AnalyticsManager,
    private val userRepository: UserRepository,
    private val coroutineDispatcher: CoroutineDispatcher,
) : CurrencyRepository {
    private val coroutinScope = CoroutineScope(coroutineDispatcher)

    private val currencyCollection: StateFlow<CurrencyCodesCollection> =
        userRepository.get().map { user ->
            val lastUsed = user?.lastUsedCurrency
            CurrencyCodesCollection(
                lru = (listOf(lastUsed) + listOf("USD", "EUR", "GBP", "CHF", "CAD").filter { it != lastUsed }).filterNotNull(),
                all = currencySymbols.keys.toList(),
            )
        }.stateIn(
            scope = coroutinScope,
            started = SharingStarted.Lazily,
            initialValue =
                CurrencyCodesCollection(
                    lru = listOf("USD", "EUR", "GBP", "CHF", "CAD"),
                    all = currencySymbols.keys.toList(),
                ),
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val fxState =
        userRepository.get().filterNotNull().flatMapLatest { user ->
            if (user.isPlus()) {
                Firebase.firestore.collection(FX_COLLECTION).document(FX_LATEST).snapshots.map {
                    if (it.exists) {
                        val rates = it.data(FxRates.serializer(), ServerTimestampBehavior.ESTIMATE)
                        FxState.Data(rates)
                    } else {
                        val exception = NullPointerException("No fx rates found")
                        analyticsManager.log(exception)
                        FxState.Error(FxState.Error.Type.FETCH_ERROR)
                    }
                }.retryWhen { cause, attempt ->
                    if (attempt > 3) return@retryWhen false
                    cause.printStackTrace()
                    analyticsManager.log(cause)
                    return@retryWhen true
                }
            } else {
                flow<FxState> { emit(FxState.Error(FxState.Error.Type.PLUS_NEEDED)) }
            }
        }.stateIn(
            scope = coroutinScope,
            started = SharingStarted.Lazily,
            initialValue = FxState.Loading,
        )

    override fun getFxRates(): StateFlow<FxState> = fxState

    override fun getAvailableCurrencyCodes(): StateFlow<CurrencyCodesCollection> = currencyCollection
}
