package app.wesplit.participant

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.wesplit.domain.model.group.GroupRepository
import app.wesplit.domain.model.group.Participant
import app.wesplit.domain.model.user.ContactListDelegate
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveButton
import io.github.alexzhirkevich.cupertino.adaptive.ExperimentalAdaptiveApi
import io.github.alexzhirkevich.cupertino.adaptive.icons.AccountBox
import io.github.alexzhirkevich.cupertino.adaptive.icons.AdaptiveIcons
import io.github.alexzhirkevich.cupertino.adaptive.icons.Done
import io.github.alexzhirkevich.cupertino.adaptive.icons.Info
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.close_picker
import split.composeapp.generated.resources.contacts_in_book
import split.composeapp.generated.resources.contacts_in_wesplit
import split.composeapp.generated.resources.create_new_contact
import split.composeapp.generated.resources.grant_permission
import split.composeapp.generated.resources.search_contact
import split.composeapp.generated.resources.start_type_creat_contact
import split.composeapp.generated.resources.user_already_in_group

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAdaptiveApi::class)
@Composable
internal fun ParticipantPicker(
    currentParticipants: Set<Participant>,
    isFullScreen: Boolean = false,
    onPickerClose: () -> Unit,
    onParticipantClick: (Participant) -> Unit,
) {
    val groupRepository: GroupRepository = koinInject()
    val contactListDelegate: ContactListDelegate = koinInject()
    val coroutineScope = rememberCoroutineScope()

    var closeButtonVisibility by remember { mutableStateOf(false) }

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
        modifier = Modifier,
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
                        imageVector = AdaptiveIcons.Outlined.AccountBox,
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

        val participantClickHandler: (Participant) -> Unit = {
            closeButtonVisibility = true
            onParticipantClick(it)
        }

        Box(modifier = Modifier.fillMaxSize(1f)) {
            when (val state = suggestions.value) {
                ParticipantPickerViewModel.State.Loading -> CircularProgressIndicator()
                is ParticipantPickerViewModel.State.Suggestions ->
                    LazyColumn(
                        state = lazyColumnListState,
                    ) {
                        newParticipantItem(state, currentParticipants, participantClickHandler)
                        currentConnectionsItem(state, currentParticipants, participantClickHandler)
                        contatctItem(state, currentParticipants, participantClickHandler)
                        item { Spacer(modifier = Modifier.navigationBarsPadding()) }
                        item {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = closeButtonVisibility,
                            ) {
                                Spacer(modifier = Modifier.height(76.dp))
                            }
                        }
                    }
            }

            androidx.compose.animation.AnimatedVisibility(
                modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 8.dp).widthIn(min = 120.dp),
                visible = closeButtonVisibility,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                AdaptiveButton(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onClick = {
                        closeButtonVisibility = false
                        coroutineScope.launch {
                            sheetState.hide()
                            onPickerClose()
                        }
                    },
                ) {
                    Text(stringResource(Res.string.close_picker))
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
            items(items = state.contacts.data, key = { (it.user?.name ?: "") + it.name }) { participant ->
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

        items(items = state.connections, key = { (it.user?.id ?: "") + it.name }) { participant ->
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
                    AdaptiveIcons.Outlined.Info,
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
                    AdaptiveIcons.Outlined.Done,
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
