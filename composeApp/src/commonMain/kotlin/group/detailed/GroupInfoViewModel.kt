package group.detailed

import RightPane
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.wesplit.domain.model.group.Group
import app.wesplit.domain.model.group.GroupRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent

class GroupInfoViewModel(
    savedStateHandle: SavedStateHandle,
    private val groupRepository: GroupRepository,
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel(),
    KoinComponent {

    val dataState: StateFlow<State>
        get() = _dataState

    private val groupId: String = checkNotNull(savedStateHandle[RightPane.Group.Param.GROUP_ID.paramName])
    private val _dataState = MutableStateFlow<State>(State.Loading)

    init {
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        withContext(ioDispatcher) {
            val group = groupRepository.get(groupId)
            _dataState.update {
                State.GroupInfo(group)
            }
        }
    }

    sealed interface State {
        data object Loading : State
        data class GroupInfo(val group: Group) : State
    }
}
