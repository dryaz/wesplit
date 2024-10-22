package app.wesplit.paywall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.wesplit.domain.model.paywall.Offer
import app.wesplit.domain.model.paywall.PaywallRepository
import app.wesplit.domain.model.paywall.Subscription
import app.wesplit.domain.model.user.UserRepository
import app.wesplit.domain.model.user.isPlus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class PaywallViewModel(
    private val paywallRepository: PaywallRepository,
    private val coroutineDispatcher: CoroutineDispatcher,
    private val userRepository: UserRepository,
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
