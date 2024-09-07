package app.wesplit.participant

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.wesplit.domain.model.group.GroupRepository
import app.wesplit.domain.model.group.Participant
import app.wesplit.domain.model.user.ContactListDelegate
import kotlinx.coroutines.Dispatchers
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.contacts_in_book
import split.composeapp.generated.resources.contacts_in_wesplit
import split.composeapp.generated.resources.create_new_contact
import split.composeapp.generated.resources.grant_permission
import split.composeapp.generated.resources.search_contact
import split.composeapp.generated.resources.start_type_creat_contact
import split.composeapp.generated.resources.user_already_in_group

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ParticipantPicker(
    currentParticipants: Set<Participant>,
    isFullScreen: Boolean = false,
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
    // TODO: Platform delegate to request access to contacts to get contacts
    // TODO: Good point for linkedin post how to create user picker in KMP compose
    var searchText =
        viewModel.searchText.collectAsState(
            context = Dispatchers.Main.immediate,
        )
    var suggestions = viewModel.suggestions.collectAsState()

    val lazyColumnListState = rememberLazyListState()
    val sheetState: SheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = isFullScreen,
        )

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { onPickerClose() },
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(1f).padding(horizontal = 16.dp),
            singleLine = true,
            value = searchText.value,
            onValueChange = { value -> viewModel.search(value) },
            prefix = {
                Row {
                    Icon(
                        imageVector = Icons.Outlined.AccountBox,
                        contentDescription =
                            stringResource(
                                Res.string.search_contact,
                            ),
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }
            },
            placeholder = {
                Text(
                    text = stringResource(Res.string.search_contact),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
        )

        Box(modifier = Modifier.fillMaxSize(1f)) {
            when (val state = suggestions.value) {
                ParticipantPickerViewModel.State.Loading -> CircularProgressIndicator()
                is ParticipantPickerViewModel.State.Suggestions ->
                    LazyColumn(
                        state = lazyColumnListState,
                    ) {
                        newParticipantItem(state, currentParticipants, onParticipantClick)
                        currentConnectionsItem(state, currentParticipants, onParticipantClick)
                        contatctItem(state, currentParticipants, onParticipantClick)
                    }
            }
        } // TODO: Loading?
    }
}

private fun LazyListScope.contatctItem(
    state: ParticipantPickerViewModel.State.Suggestions,
    currentParticipants: Set<Participant>,
    onParticipantClick: (Participant) -> Unit,
) {
    if (state.contacts !is ContactListDelegate.State.NotSuppoted) {
        item {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).padding(top = 16.dp),
                text = stringResource(Res.string.contacts_in_book),
                style = MaterialTheme.typography.titleSmall,
            )
        }

        if (state.contacts is ContactListDelegate.State.PermissionRequired) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    OutlinedButton(
                        modifier = Modifier.padding(top = 8.dp),
                        onClick = { TODO("Request contact permission") },
                    ) {
                        Text(text = stringResource(Res.string.grant_permission))
                    }
                }
            }
        } else if (state.contacts is ContactListDelegate.State.Contacts) {
            items(items = state.contacts.data, key = { (it.id ?: "") + it.name }) { participant ->
                ParticipantPickerItem(participant, currentParticipants, onParticipantClick)
                HorizontalDivider(modifier = Modifier.padding(start = 64.dp))
            }
        }
    }
}

private fun LazyListScope.currentConnectionsItem(
    state: ParticipantPickerViewModel.State.Suggestions,
    currentParticipants: Set<Participant>,
    onParticipantClick: (Participant) -> Unit,
) {
    if (state.connections.isNotEmpty()) {
        item {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).padding(top = 16.dp),
                text = stringResource(Res.string.contacts_in_wesplit),
                style = MaterialTheme.typography.titleSmall,
            )
        }

        items(items = state.connections, key = { (it.id ?: "") + it.name }) { participant ->
            ParticipantPickerItem(participant, currentParticipants, onParticipantClick)
            HorizontalDivider(modifier = Modifier.padding(start = 64.dp))
        }
    }
}

private fun LazyListScope.newParticipantItem(
    state: ParticipantPickerViewModel.State.Suggestions,
    currentParticipants: Set<Participant>,
    onParticipantClick: (Participant) -> Unit,
) {
    item {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).padding(top = 16.dp),
            text = stringResource(Res.string.create_new_contact),
            style = MaterialTheme.typography.titleSmall,
        )
        if (state.newParticipant != null) {
            ParticipantPickerItem(state.newParticipant, currentParticipants, onParticipantClick)
        } else {
            Row(
                modifier = Modifier.fillMaxHeight(1f).padding(horizontal = 16.dp, vertical = 8.dp).padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = stringResource(Res.string.start_type_creat_contact),
                    tint = MaterialTheme.colorScheme.secondary,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(Res.string.start_type_creat_contact),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ParticipantPickerItem(
    participant: Participant,
    currentParticipants: Set<Participant>,
    onParticipantClick: (Participant) -> Unit,
) {
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
}
