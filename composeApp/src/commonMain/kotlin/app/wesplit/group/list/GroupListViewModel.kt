package app.wesplit.group.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.wesplit.domain.model.account.Account
import app.wesplit.domain.model.account.AccountRepository
import app.wesplit.domain.model.group.Group
import app.wesplit.domain.model.group.GroupRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class GroupListViewModel(
    private val accountRepository: AccountRepository,
    private val groupRepository: GroupRepository,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel(),
    KoinComponent {
    val dataState: StateFlow<State>
        get() = _dataState

    val accountState: StateFlow<Account>
        get() = accountRepository.get()

    private val _dataState = MutableStateFlow<State>(State.Empty)

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            groupRepository.get().collectLatest { groups ->
                _dataState.update {
                    if (groups.isEmpty()) {
                        State.Empty
                    } else {
                        State.Groups(groups)
                    }
                }
            }
        }
    }

    sealed interface State {
        data object Empty : State

        data class Groups(
            val groups: List<Group>,
        ) : State
    }
}
