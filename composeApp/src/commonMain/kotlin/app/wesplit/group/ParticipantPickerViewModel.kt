package app.wesplit.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.wesplit.domain.model.group.GroupRepository
import app.wesplit.domain.model.group.Participant
import app.wesplit.domain.model.user.ContactListDelegate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import kotlin.time.Duration.Companion.milliseconds

// TODO: Find user connections
// TODO: Create platform delegate to return list of users
// TODO: Sort users by recent?
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class ParticipantPickerViewModel(
    private val groupRepository: GroupRepository,
    private val contactListDelegate: ContactListDelegate,
) : ViewModel(), KoinComponent {
    val searchText = MutableStateFlow("")
    val suggestions = MutableStateFlow<State>(State.Loading)

    init {
        viewModelScope.launch {
            searchText.debounce(75.milliseconds).flatMapLatest { query ->
                groupRepository.getSuggestedParticipants(query)
            }.collectLatest { participants ->
                suggestions.update { State.Suggestions(participants) }
            }
        }
    }

    fun search(query: String) {
        searchText.update { query }
    }

    sealed interface State {
        data object Loading : State

        data class Suggestions(val participants: List<Participant>) : State
    }
}
