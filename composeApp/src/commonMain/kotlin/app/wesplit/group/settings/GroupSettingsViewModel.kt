package app.wesplit.group.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.wesplit.domain.model.account.Account
import app.wesplit.domain.model.account.AccountRepository
import app.wesplit.domain.model.account.participant
import app.wesplit.domain.model.exception.UnauthorizeAcceessException
import app.wesplit.domain.model.group.GroupRepository
import app.wesplit.domain.model.group.Participant
import app.wesplit.routing.RightPane
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class GroupSettingsViewModel(
    savedStateHandle: SavedStateHandle,
    private val groupRepository: GroupRepository,
    private val accountRepository: AccountRepository,
) : ViewModel(), KoinComponent {
    // TODO: savedStateHandle should be used to support same settings screen for existing group.
    val groupId: String? =
        savedStateHandle[
            RightPane
                .GroupSettings
                .Param
                .GROUP_ID
                .paramName,
        ]

    val state: StateFlow<State>
        get() = _state

    private var loadJob: Job? = null
    private val _state = MutableStateFlow<State>(State.Loading)

    init {
        if (groupId != null) {
            reload()
        } else {
            _state.update { emptyGroupState() }
        }
    }

    fun commit() =
        with(state.value as State.Group) {
            viewModelScope.launch {
                groupRepository.commit(id, title, participants)
            }
        }

    // TODO: MVI appraoch like in ExpenseDetailsViewModel
    fun leave() {
        with(state.value as State.Group) {
            viewModelScope.launch {
                id?.let {
                    groupRepository.leave(it)
                }
            }
        }
    }

    fun update(group: State.Group) = _state.update { group }

    // TODO: Check if we need reload with firebase or it will automatically return data without reloading.
    fun reload() {
        if (groupId == null) throw IllegalStateException("Can't reload group without ID")
        loadJob?.cancel()
        loadJob =
            viewModelScope.launch {
                groupRepository.get(groupId).collectLatest { groupResult ->
                    val exception = groupResult.exceptionOrNull()
                    _state.update {
                        when (exception) {
                            is UnauthorizeAcceessException -> State.Error(State.Error.Type.UNAUTHORIZED)
                            is NullPointerException -> State.Error(State.Error.Type.NOT_EXISTS)
                            else ->
                                if (exception != null) {
                                    State.Error(State.Error.Type.FETCH_ERROR)
                                } else {
                                    with(groupResult.getOrThrow()) {
                                        State.Group(
                                            id = this.id,
                                            title = this.title,
                                            participants = this.participants,
                                        )
                                    }
                                }
                        }
                    }
                }
            }
    }

    private fun emptyGroupState() =
        State.Group(
            id = null,
            title = "",
            participants =
                linkedSetOf(
                    (accountRepository.get().value as? Account.Authorized)?.participant(),
                ).filterNotNull().toSet(),
        )

    sealed interface State {
        data object Loading : State

        data class Error(val type: Type) : State {
            enum class Type {
                NOT_EXISTS,
                UNAUTHORIZED,
                FETCH_ERROR,
            }
        }

        data class Group(
            // TODO: Support image
            val id: String?,
            val title: String,
            val participants: Set<Participant>,
        ) : State
    }
}
