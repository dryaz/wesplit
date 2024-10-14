package app.wesplit.paywall

import androidx.lifecycle.ViewModel
import app.wesplit.domain.model.paywall.PaywallRepository
import app.wesplit.domain.model.paywall.Subscription
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class PaywallViewModel(
    private val paywallRepository: PaywallRepository,
    private val coroutineDispatcher: CoroutineDispatcher,
) : ViewModel(), KoinComponent {
    val state: StateFlow<State>
        get() = _state

    private val coroutinScope = CoroutineScope(coroutineDispatcher)
    private val _state = MutableStateFlow<State>(State.Loading)

    init {
        refresh()
    }

    private fun refresh() =
        coroutinScope.launch {
            if (!paywallRepository.isBillingSupported())
                {
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

    sealed interface State {
        data object Loading : State

        data object Error : State

        data object BillingNotSupported : State

        data class Data(val products: List<Subscription>) : State
    }
}
