package app.wesplit.group

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.wesplit.domain.model.group.GroupRepository
import app.wesplit.domain.model.group.Participant
import app.wesplit.domain.model.user.ContactListDelegate
import kotlinx.coroutines.Dispatchers
import org.koin.compose.koinInject

// TODO: Ux improvement - add multiple users from single bottom sheet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ParticipantPicker(onUserSelect: (Participant?) -> Unit) {
    val groupRepository: GroupRepository = koinInject()
    val contactListDelegate: ContactListDelegate = koinInject()

    val viewModel =
        viewModel {
            ParticipantPickerViewModel(groupRepository, contactListDelegate)
        }

    // TODO: Search user, last option is to create new user with name as current input in search
    // TODO: Empty state, if no yet users show img and description that start typing to create contact
    // TODO: Platform delegate to request access to contacts to get contacts
    // TODO: Good point for linkedin post how to create user picker in KMP compose
    var searchText =
        viewModel.searchText.collectAsState(
            context = Dispatchers.Main.immediate,
        )
    var suggestions = viewModel.suggestions.collectAsState()
    val participants =
        remember {
            derivedStateOf {
                (suggestions.value as? ParticipantPickerViewModel.State.Suggestions)?.participants
            }
        }
    val lazyColumnListState = rememberLazyListState()

    // TODO: Min sheet size. Is it possible not to jump during filtering by text?
    ModalBottomSheet(
        onDismissRequest = { onUserSelect(null) },
    ) {
        // TODO: Padding/styling
        OutlinedTextField(
            value = searchText.value,
            onValueChange = { value -> viewModel.search(value) },
        )
        AnimatedVisibility(visible = participants.value != null) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = lazyColumnListState,
            ) {
                items(items = participants.value ?: emptyList(), key = { it.name }) { participant ->
                    ParticipantListItem(participant) {
                        onUserSelect(it)
                    }
                    HorizontalDivider(modifier = Modifier.padding(start = 64.dp))
                }
            }
        }
        // TODO: Loading
    }

    // TODO: Fire callback after modal is closed first to improve UX
}
