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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
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

    private val dataState = MutableStateFlow<DataState>(DataState.Loading)
    private var loadJob: Job? = null

    init {
        if (groupId != null) {
            reload()
        } else {
            dataState.update { emptyGroupState() }
        }
    }

    val state: StateFlow<UiState> =
        combine(dataState, accountRepository.get()) { value1, value2 ->
            UiState(value1, value2)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue =
                UiState(
                    DataState.Loading,
                    Account.Unknown,
                ),
        )

    fun commit() =
        with(dataState.value as DataState.Group) {
            viewModelScope.launch {
                groupRepository.commit(id, title, participants)
            }
        }

    // TODO: MVI appraoch like in ExpenseDetailsViewModel
    fun leave() {
        with(dataState.value as DataState.Group) {
            viewModelScope.launch {
                id?.let {
                    groupRepository.leave(it)
                }
            }
        }
    }

    fun join(asParticipant: Participant?) {
        with(dataState.value as DataState.Group) {
            viewModelScope.launch {
                val newParticipants =
                    if (asParticipant == null) {
                        val newParticipant = accountRepository.get().first { it is Account.Authorized }.participant()
                        if (newParticipant != null) {
                            participants + newParticipant
                        } else {
                            participants
                        }
                    } else {
                        val me = (accountRepository.get().first { it is Account.Authorized } as Account.Authorized).user
                        participants.map {
                            if (it.id != asParticipant.id) it else it.copy(user = me)
                        }
                    }

                groupRepository.commit(id, title, newParticipants.toSet())
            }
        }
    }

    fun update(group: DataState.Group) = dataState.update { group }

    // TODO: Check if we need reload with firebase or it will automatically return data without reloading.
    fun reload() {
        if (groupId == null) throw IllegalStateException("Can't reload group without ID")
        loadJob?.cancel()
        loadJob =
            viewModelScope.launch {
                groupRepository.get(groupId).collectLatest { groupResult ->
                    val exception = groupResult.exceptionOrNull()
                    dataState.update {
                        when (exception) {
                            is UnauthorizeAcceessException -> DataState.Error(DataState.Error.Type.UNAUTHORIZED)
                            is NullPointerException -> DataState.Error(DataState.Error.Type.NOT_EXISTS)
                            else ->
                                if (exception != null) {
                                    DataState.Error(DataState.Error.Type.FETCH_ERROR)
                                } else {
                                    with(groupResult.getOrThrow()) {
                                        DataState.Group(
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
        DataState.Group(
            id = null,
            title = "",
            participants =
                linkedSetOf(
                    (accountRepository.get().value as? Account.Authorized)?.participant(),
                ).filterNotNull().toSet(),
        )

    data class UiState(
        val dataState: DataState,
        val account: Account,
    )

    sealed interface DataState {
        data object Loading : DataState

        data class Error(val type: Type) : DataState {
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
        ) : DataState
    }
}
