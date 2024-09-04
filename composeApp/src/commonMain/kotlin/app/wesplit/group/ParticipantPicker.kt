package app.wesplit.group

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.wesplit.domain.model.group.GroupRepository
import app.wesplit.domain.model.group.Participant
import app.wesplit.domain.model.user.ContactListDelegate
import kotlinx.coroutines.Dispatchers
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.img_search_empty
import split.composeapp.generated.resources.no_contact_found
import split.composeapp.generated.resources.search_contact
import split.composeapp.generated.resources.user_already_in_group

// TODO: Ux improvement - add multiple users from single bottom sheet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ParticipantPicker(
    currentParticipants: Set<Participant>,
    onPickerClose: () -> Unit,
    onParticipantClick: (Participant) -> Unit,
) {
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
    val sheetState: SheetState = rememberModalBottomSheetState()

    // TODO: Min sheet size. Is it possible not to jump during filtering by text?
    // TODO: (Idea) Add search to the bottom so even collapsing items won't affect users' UX
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { onPickerClose() },
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(1f).padding(horizontal = 16.dp),
            value = searchText.value,
            onValueChange = { value -> viewModel.search(value) },
            prefix = { Icon(Icons.Filled.Search, stringResource(Res.string.search_contact)) },
            placeholder = {
                Text(
                    text = stringResource(Res.string.search_contact),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
        )
        val anyParticipants = (participants.value?.size ?: 0) > 0
        AnimatedVisibility(modifier = Modifier.fillMaxSize(1f), visible = anyParticipants) {
            LazyColumn(
                state = lazyColumnListState,
            ) {
                items(items = participants.value ?: emptyList(), key = { it.name }) { participant ->
                    ParticipantListItem(
                        participant = participant,
                        action = {
                            if (participant in currentParticipants) {
                                Icon(
                                    Icons.Filled.Done,
                                    contentDescription = stringResource(Res.string.user_already_in_group),
                                )
                            } else {
                                Unit
                            }
                        },
                    ) {
                        onParticipantClick(it)
                    }
                    HorizontalDivider(modifier = Modifier.padding(start = 64.dp))
                }
            }
        }
        AnimatedVisibility(modifier = Modifier.fillMaxSize(1f), visible = !anyParticipants) {
            Column(
                modifier = Modifier.padding(top = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    modifier = Modifier,
                    painter = painterResource(Res.drawable.img_search_empty),
                    contentDescription = stringResource(Res.string.no_contact_found),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(Res.string.no_contact_found),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        // TODO: Loading
    }
}
