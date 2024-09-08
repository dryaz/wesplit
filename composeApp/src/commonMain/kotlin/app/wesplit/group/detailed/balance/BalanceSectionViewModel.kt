package app.wesplit.group.detailed.balance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.wesplit.domain.model.group.balance.Balance
import app.wesplit.domain.model.group.balance.BalanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class BalanceSectionViewModel(
    private val groupId: String,
    private val balanceRepository: BalanceRepository,
) : ViewModel(),
    KoinComponent {
    val dataState: StateFlow<State>
        get() = _dataState

    private val _dataState = MutableStateFlow<State>(State.Loading)

    init {
        refresh()
    }

    // TODO: We show only balances for ppl which are not settled, should probably show all ppl with 0 balance
    fun refresh() {
        viewModelScope.launch {
            balanceRepository.getByGroupId(groupId).collectLatest { balance ->
                _dataState.update {
                    State.Data(balance)
                }
            }
        }
    }

    sealed interface State {
        data object Loading : State

        data object Empty : State

        data object Unauthorized : State

        data class Data(
            val balance: Balance?,
        ) : State
    }
}
