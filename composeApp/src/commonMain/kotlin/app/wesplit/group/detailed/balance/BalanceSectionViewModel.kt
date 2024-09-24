package app.wesplit.group.detailed.balance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.LogLevel
import app.wesplit.domain.model.group.balance.Balance
import app.wesplit.domain.model.group.balance.BalanceRepository
import app.wesplit.group.detailed.GroupInfoViewModel.State
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class BalanceSectionViewModel(
    private val groupId: String,
    private val balanceRepository: BalanceRepository,
    private val analyticsManager: AnalyticsManager,
) : ViewModel(), KoinComponent {
    val dataState: StateFlow<State>
        get() = _dataState

    private val _dataState = MutableStateFlow<State>(State.Loading)

    init {
        refresh()
    }

    // TODO: We show only balances for ppl which are not settled, should probably show all ppl with 0 balance
    fun refresh() {
        viewModelScope.launch {
            balanceRepository.getByGroupId(groupId)
                .catch {
                    analyticsManager.log("BalanceSectionViewModel - refresh()", LogLevel.WARNING)
                    analyticsManager.log(it)
                    // TODO: Improve error handling, e.g. get reason and plot proper data
                    _dataState.update { State.Error }
                }
                .collectLatest { balanceResult ->
                    if (balanceResult.isFailure) {
                        _dataState.update { State.Error }
                    } else {
                        _dataState.update {
                            State.Data(balanceResult.getOrThrow())
                        }
                    }
                }
        }
    }

    sealed interface State {
        data object Loading : State

        data object Error : State

        data class Data(
            val balance: Balance?,
        ) : State
    }
}
