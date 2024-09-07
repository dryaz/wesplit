package app.wesplit.group.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import app.wesplit.participant.ParticipantListItem
import app.wesplit.participant.ParticipantPicker
import app.wesplit.ui.AdaptiveTopAppBar
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveIconButton
import io.github.alexzhirkevich.cupertino.adaptive.ExperimentalAdaptiveApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.add_user_to_group
import split.composeapp.generated.resources.create
import split.composeapp.generated.resources.group_name
import split.composeapp.generated.resources.ic_user_add
import split.composeapp.generated.resources.loading
import split.composeapp.generated.resources.new_group
import split.composeapp.generated.resources.retry
import split.composeapp.generated.resources.save
import split.composeapp.generated.resources.settings

sealed interface GroupSettingsAction {
    data object Back : GroupSettingsAction
}

private sealed interface GroupSettingTollbarAction {
    data object Reload : GroupSettingTollbarAction

    data object Commit : GroupSettingTollbarAction
}

@Composable
fun GroupSettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: GroupSettingsViewModel,
    onAction: (GroupSettingsAction) -> Unit,
) {
    val state = viewModel.state.collectAsState()

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            TopAppBareByState(
                state = state.value,
                onAction = onAction,
                onToolbarAction = { action ->
                    when (action) {
                        GroupSettingTollbarAction.Commit -> {
                            viewModel.commit()
                            onAction(GroupSettingsAction.Back)
                        }

                        GroupSettingTollbarAction.Reload -> viewModel.reload()
                    }
                },
            )
        },
    ) { paddings ->
        when (val groupState = state.value) {
            is GroupSettingsViewModel.State.Error -> Text("Error")
            is GroupSettingsViewModel.State.Group ->
                GroupSettingsView(
                    modifier = Modifier.fillMaxSize(1f).padding(paddings),
                    group = groupState,
                    onDone = {
                        viewModel.commit()
                        onAction(GroupSettingsAction.Back)
                    },
                ) { group ->
                    viewModel.update(group)
                }
            // TODO: Shimmer?
            GroupSettingsViewModel.State.Loading -> Text("Loading")
        }
    }
}

@OptIn(ExperimentalAdaptiveApi::class)
@Composable
private fun GroupSettingsView(
    modifier: Modifier = Modifier,
    group: GroupSettingsViewModel.State.Group,
    onDone: () -> Unit,
    onUpdated: (GroupSettingsViewModel.State.Group) -> Unit,
) {
    var userSelectorVisibility by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier =
            modifier
                .padding(top = 16.dp)
                .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
            modifier =
                Modifier
                    .widthIn(max = 450.dp)
                    .fillMaxWidth(1f)
                    .padding(horizontal = 16.dp),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // TODO: Support adding image, place pic selector in here
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(1f),
                    // TODO: Prefil later when select paritipatns if empty
                    value = group.title,
                    onValueChange = { onUpdated(group.copy(title = it)) },
                    label = {
                        Text(stringResource(Res.string.group_name))
                    },
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions =
                        KeyboardOptions(
                            imeAction = ImeAction.Next,
                        ),
                    keyboardActions =
                        KeyboardActions {
                            onDone()
                        },
                )
            }
        }

        Card(
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
            modifier =
                Modifier
                    .widthIn(max = 450.dp)
                    .fillMaxWidth(1f)
                    .padding(16.dp),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth(1f)
                        .clickable {
                            userSelectorVisibility = true
                        }.padding(16.dp),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_user_add),
                    modifier = Modifier.width(48.dp),
                    contentDescription = stringResource(Res.string.add_user_to_group),
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(Res.string.add_user_to_group),
                )
            }

            // TODO: Add/Remove with animation. Lazycolumn?
            group.participants.forEachIndexed { index, user ->
                HorizontalDivider(
                    modifier = Modifier.padding(start = if (index == 0) 0.dp else 80.dp),
                )
                ParticipantListItem(
                    participant = user,
                    action =
                        if (!user.isMe) {
                            {
                                AdaptiveIconButton(onClick = { onUpdated(group.copy(participants = group.participants - user)) }) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        // TODO: Proper CD
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        } else {
                            null
                        },
                )
            }
        }
    }

    AnimatedVisibility(visible = userSelectorVisibility) {
        ParticipantPicker(
            currentParticipants = group.participants,
            isFullScreen = true,
            onPickerClose = { userSelectorVisibility = false },
        ) { user ->
            if (user in group.participants) {
                onUpdated(group.copy(participants = group.participants - user))
            } else {
                onUpdated(group.copy(participants = group.participants + user))
            }
        }
    }
}

@Composable
private fun TopAppBareByState(
    state: GroupSettingsViewModel.State,
    onAction: (GroupSettingsAction) -> Unit,
    onToolbarAction: (GroupSettingTollbarAction) -> Unit,
) {
    AdaptiveTopAppBar(
        title = {
            Text(
                stringResource(
                    when (state) {
                        GroupSettingsViewModel.State.Loading -> Res.string.loading
                        is GroupSettingsViewModel.State.Error -> Res.string.settings
                        is GroupSettingsViewModel.State.Group ->
                            if (state.id == null) {
                                Res.string.new_group
                            } else {
                                Res.string.settings
                            }
                    },
                ),
            )
        },
        onNavigationIconClick = { onAction(GroupSettingsAction.Back) },
        actions = {
            Box(
                modifier =
                    Modifier.fillMaxHeight(1f).clickable {
                        when (state) {
                            is GroupSettingsViewModel.State.Error ->
                                onToolbarAction(
                                    GroupSettingTollbarAction.Reload,
                                )

                            is GroupSettingsViewModel.State.Group ->
                                onToolbarAction(
                                    GroupSettingTollbarAction.Commit,
                                )

                            GroupSettingsViewModel.State.Loading -> {}
                        }
                    }.padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                when (state) {
                    is GroupSettingsViewModel.State.Error ->
                        Text(
                            // TODO: Add leading icon retry icon
                            text = stringResource(Res.string.retry),
                        )

                    is GroupSettingsViewModel.State.Group ->
                        if (state.id == null) {
                            // TODO: Add leading icon OK
                            Text(
                                text = stringResource(Res.string.create),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        } else {
                            Text(
                                text = stringResource(Res.string.save),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }

                    GroupSettingsViewModel.State.Loading -> CircularProgressIndicator()
                }
            }
        },
    )
}
