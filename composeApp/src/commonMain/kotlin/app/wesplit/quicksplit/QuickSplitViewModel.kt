package app.wesplit.quicksplit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.AppReviewManager
import app.wesplit.domain.model.currency.Amount
import app.wesplit.domain.model.currency.CurrencyRepository
import app.wesplit.domain.model.user.UserRepository
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

sealed interface UpdateAction

class QuickSplitViewModel(
    private val currencyRepository: CurrencyRepository,
    private val analyticsManager: AnalyticsManager,
    private val settings: Settings,
    private val appReviewManager: AppReviewManager,
    private val userRepository: UserRepository,
) : ViewModel(), KoinComponent {
    val state: StateFlow<State>
        get() = _state

    private val _state = MutableStateFlow<State>(State.Loading)

    init {
        viewModelScope.launch {
        }
    }

    fun update(action: UpdateAction) {
    }

    sealed interface State {
        data object Loading : State

        data class Data(
            val amount: Amount,
        ) : State
    }
}
