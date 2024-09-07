package app.wesplit.participant

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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import kotlin.time.Duration.Companion.milliseconds

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
            searchText.debounce(150.milliseconds).flatMapLatest { query ->
                groupRepository.getSuggestedParticipants(query).map { it to query }
            }.collectLatest { data ->
                val query = data.second
                val connections = data.first
                val newParticipant = if (query.isNotBlank()) Participant(name = query) else null
                val contacts = contactListDelegate.get(query)
                val suggestions =
                    State.Suggestions(
                        newParticipant = newParticipant,
                        connections = connections,
                        contacts = contacts,
                    )

                this@ParticipantPickerViewModel.suggestions.update { suggestions }
            }
        }
    }

    fun search(query: String) {
        searchText.update { query }
    }

    sealed interface State {
        data object Loading : State

        data class Suggestions(
            val newParticipant: Participant?,
            val connections: List<Participant>,
            val contacts: ContactListDelegate.State,
        ) : State
    }
}
