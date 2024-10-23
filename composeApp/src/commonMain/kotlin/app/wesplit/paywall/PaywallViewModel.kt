package app.wesplit.paywall

import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.paywall.Offer
import app.wesplit.domain.model.paywall.PaywallRepository
import app.wesplit.domain.model.paywall.Subscription
import app.wesplit.domain.model.user.UserRepository
import app.wesplit.domain.model.user.isPlus
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.functions.functions
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

private const val PROMO_APPLY = "promo_apply"
private const val PROMO_APPLY_SUCCESS = "promo_success"
private const val PROMO_APPLY_FAILED = "promo_failed"

private const val PROMO_CODE_PARAM = "code"

class PaywallViewModel(
    private val paywallRepository: PaywallRepository,
    private val coroutineDispatcher: CoroutineDispatcher,
    private val userRepository: UserRepository,
    private val analyticsManager: AnalyticsManager,
) : ViewModel(), KoinComponent {
    val state: StateFlow<State>
        get() = _state

    val event: Flow<Event>
        get() = _event.receiveAsFlow()

    private val coroutinScope = CoroutineScope(coroutineDispatcher)

    private val _state = MutableStateFlow<State>(State.Loading)
    private val _event = Channel<Event>(Channel.BUFFERED)

    init {
        refresh()
    }

    private fun refresh() =
        coroutinScope.launch {
            if (userRepository.get().value?.isPlus() == true) {
                _state.update { State.AlreadySubscribed }
                return@launch
            }
            if (!paywallRepository.isBillingSupported()) {
                _state.update { State.BillingNotSupported }
                return@launch
            }
            val result = paywallRepository.getProducts()
            if (result.isFailure) {
                _state.update { State.Error }
                return@launch
            }

            _state.update { State.Data(result.getOrThrow()) }
        }

    fun subscribe(subscription: Subscription) {
        viewModelScope.launch {
            val result = paywallRepository.subscribe(subscription.period)
            if (result.isSuccess) {
                _event.send(Event.Purchased)
            } else {
                _event.send(Event.Error(result.exceptionOrNull()?.message ?: "Can't perform purchase now"))
            }
        }
    }

    fun applyPromocode(code: String) =
        viewModelScope.launch(NonCancellable) {
            val codeEventParam = mapOf(PROMO_CODE_PARAM to code)
            analyticsManager.track(PROMO_APPLY, codeEventParam)
            // Get an instance of Firebase Functions
            val functions = Firebase.functions

            // Get a reference to the 'generateGroupToken' callable function
            val applyPromo = functions.httpsCallable("applyPromoCode")

            try {
                // Call the Cloud Function with the required data
                applyPromo.invoke(mapOf("promo" to code.trim().toLowerCase(Locale.current)))
                analyticsManager.track(PROMO_APPLY_SUCCESS, codeEventParam)
                _event.send(Event.Purchased)
            } catch (e: Exception) {
                analyticsManager.track(PROMO_APPLY_FAILED, codeEventParam)
                analyticsManager.log(e)
                _event.send(Event.Error("Promocode is not valid"))
            }
        }

    sealed interface State {
        data object Loading : State

        data object Error : State

        data object BillingNotSupported : State

        data object AlreadySubscribed : State

        data class Data(val products: List<Pair<Subscription, Offer>>) : State
    }

    sealed interface Event {
        data object Purchased : Event

        data class Error(val msg: String) : Event
    }
}
