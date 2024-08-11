package group.detailed

import RightPane
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.wesplit.domain.model.exception.UnauthorizeAcceessException
import app.wesplit.domain.model.group.Group
import app.wesplit.domain.model.group.GroupRepository
import group.settings.GroupSettingsViewModel.State
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class GroupInfoViewModel(
    savedStateHandle: SavedStateHandle,
    private val groupRepository: GroupRepository,
) : ViewModel(),
    KoinComponent {
    val dataState: StateFlow<State>
        get() = _dataState

    private val groupId: String =
        checkNotNull(
            savedStateHandle[
                RightPane
                    .Group
                    .Param
                    .GROUP_ID
                    .paramName,
            ],
        )
    private val _dataState = MutableStateFlow<State>(State.Loading)

    init {
        refresh()
    }

    fun refresh() =
        viewModelScope.launch {
            groupRepository.get(groupId).collectLatest { groupResult ->
                val exception = groupResult.exceptionOrNull()
                _dataState.update {
                    when (exception) {
                        is UnauthorizeAcceessException -> GroupInfoViewModel.State.Error(GroupInfoViewModel.State.Error.Type.UNAUTHORIZED)
                        is NullPointerException -> GroupInfoViewModel.State.Error(GroupInfoViewModel.State.Error.Type.NOT_EXISTS)
                        else ->
                            if (exception != null) {
                                GroupInfoViewModel.State.Error(GroupInfoViewModel.State.Error.Type.FETCH_ERROR)
                            } else {
                                GroupInfoViewModel.State.GroupInfo(groupResult.getOrThrow())
                            }
                    }
                }
            }
        }

    sealed interface State {
        data object Loading : State

        data class Error(val type: Type) : GroupInfoViewModel.State {
            enum class Type {
                NOT_EXISTS,
                UNAUTHORIZED,
                FETCH_ERROR,
            }
        }

        data class GroupInfo(
            val group: Group,
        ) : State
    }
}
