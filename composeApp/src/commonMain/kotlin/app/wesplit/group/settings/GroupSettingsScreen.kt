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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.account.Account
import app.wesplit.domain.model.group.Participant
import app.wesplit.domain.model.group.isMe
import app.wesplit.domain.model.user.OnboardingStep
import app.wesplit.participant.ParticipantListItem
import app.wesplit.participant.ParticipantPicker
import app.wesplit.ui.AdaptiveTopAppBar
import app.wesplit.ui.HelpOverlayPosition
import app.wesplit.ui.LocalTutorialControl
import app.wesplit.ui.TutorialControl
import app.wesplit.ui.TutorialItem
import app.wesplit.ui.TutorialStep
import io.github.alexzhirkevich.cupertino.adaptive.icons.AdaptiveIcons
import io.github.alexzhirkevich.cupertino.adaptive.icons.Delete
import io.github.alexzhirkevich.cupertino.adaptive.icons.ExitToApp
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

    data object Home : GroupSettingsAction
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
    val tutorialControl = LocalTutorialControl.current
    val saveGroupTutorialStep =
        remember {
            TutorialStep(
                title = "Save the group",
                description = "Now let's save what we have. You could leave group or edit anytime afterwards.",
                onboardingStep = OnboardingStep.SAVE_GROUP,
                isModal = false,
                helpOverlayPosition = HelpOverlayPosition.BOTTOM_LEFT,
            )
        }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            TopAppBareByState(
                dataState = state.value.dataState,
                onAction = onAction,
                saveTutorialStep = saveGroupTutorialStep,
                onToolbarAction = { action ->
                    when (action) {
                        GroupSettingTollbarAction.Commit -> {
                            tutorialControl.onNext()
                            viewModel.commit()
                            onAction(GroupSettingsAction.Back)
                        }

                        GroupSettingTollbarAction.Reload -> viewModel.reload()
                    }
                },
            )
        },
    ) { paddings ->
        when (val groupState = state.value.dataState) {
            is GroupSettingsViewModel.DataState.Error -> Text("Error")
            is GroupSettingsViewModel.DataState.Group ->
                GroupSettingsView(
                    modifier = Modifier.fillMaxSize(1f).padding(paddings),
                    account = state.value.account,
                    group = groupState,
                    onDone = {
                        viewModel.commit()
                        onAction(GroupSettingsAction.Back)
                    },
                    onLeave = {
                        viewModel.leave()
                        onAction(GroupSettingsAction.Home)
                    },
                    onJoin = { participant ->
                        viewModel.join(participant)
                    },
                    saveGroupTutorialStep = saveGroupTutorialStep,
                    tutorialControl = tutorialControl,
                ) { group ->
                    viewModel.update(group)
                }
            // TODO: Shimmer?
            GroupSettingsViewModel.DataState.Loading -> Text("Loading")
        }
    }
}

// TODO: Move to actions
@Composable
private fun GroupSettingsView(
    modifier: Modifier = Modifier,
    group: GroupSettingsViewModel.DataState.Group,
    account: Account,
    tutorialControl: TutorialControl,
    saveGroupTutorialStep: TutorialStep,
    onDone: () -> Unit,
    onJoin: (Participant?) -> Unit,
    onLeave: () -> Unit,
    onUpdated: (GroupSettingsViewModel.DataState.Group) -> Unit,
) {
    var userSelectorVisibility by rememberSaveable { mutableStateOf(false) }
    var leaveDialogShown by remember { mutableStateOf(false) }
    val addParticipantTutorialStep =
        remember {
            TutorialStep(
                title = "Add participant",
                description = "You could add new participant just by typing name. Your friend don't need even to know about Wesplit.",
                onboardingStep = OnboardingStep.ADD_NEW_USER_BUTTON,
                isModal = false,
                helpOverlayPosition = HelpOverlayPosition.TOP_LEFT,
            )
        }

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
                            capitalization = KeyboardCapitalization.Sentences,
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
            TutorialItem(
                onPositioned = { tutorialControl.onPositionRecieved(addParticipantTutorialStep, it) },
            ) { modifier ->
                Row(
                    modifier =
                        modifier
                            .fillMaxWidth(1f)
                            .clickable {
                                tutorialControl.onNext()
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
            }
            // TODO: Add/Remove with animation. Lazycolumn?
            val isMeParticipating = remember(group) { group.participants.any { it.isMe() } }
            group.participants.forEachIndexed { index, participant ->
                HorizontalDivider(
                    modifier = Modifier.padding(start = if (index == 0) 0.dp else 80.dp),
                )
                ParticipantListItem(
                    participant = participant,
                    action =
                        if (!isMeParticipating && participant.user?.authIds.isNullOrEmpty() && account is Account.Authorized) {
                            {
                                OutlinedButton(
                                    onClick = { onJoin(participant) },
                                ) {
                                    Text("It's me")
                                }
                            }
                        } else if (isMeParticipating && !participant.isMe() &&
                            (account is Account.Authorized || participant.user == null)
                        ) {
                            {
                                IconButton(onClick = { onUpdated(group.copy(participants = group.participants - participant)) }) {
                                    Icon(
                                        AdaptiveIcons.Outlined.Delete,
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

        if (group.id != null && account is Account.Authorized) {
            val isMeParticipating = group.participants.any { it.isMe() }
            Spacer(modifier = Modifier.height(16.dp))
            if (!isMeParticipating) {
                OutlinedButton(
                    onClick = { onJoin(null) },
                    modifier =
                        Modifier.widthIn(max = 450.dp)
                            .fillMaxWidth(1f)
                            .padding(horizontal = 16.dp),
                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                ) {
                    Text(
                        "Join as new participant",
                    )
                }
            }
            OutlinedButton(
                onClick = { leaveDialogShown = true },
                modifier =
                    Modifier.widthIn(max = 450.dp)
                        .fillMaxWidth(1f)
                        .padding(horizontal = 16.dp),
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
            ) {
                Text(
                    if (isMeParticipating) "Leave group" else "Forget group",
                )
            }
        }
    }

    AnimatedVisibility(visible = userSelectorVisibility) {
        val callback: (Participant) -> Unit =
            remember(group.participants) {
                { user ->
                    val newParticipants =
                        if (user in group.participants) {
                            group.participants - user
                        } else {
                            group.participants + user
                        }
                    println(newParticipants)
                    onUpdated(group.copy(participants = newParticipants))
                }
            }
        ParticipantPicker(
            currentParticipants = group.participants,
            isFullScreen = true,
            onPickerClose = {
                tutorialControl.stepRequest(listOf(saveGroupTutorialStep))
                userSelectorVisibility = false
            },
            onParticipantClick = callback,
        )
    }

    if (leaveDialogShown) {
        AlertDialog(
            modifier = Modifier.widthIn(max = 450.dp),
            onDismissRequest = { leaveDialogShown = false },
            title = { Text("Leave ${group.title}?") },
            text = {
                Text(
                    "Your recorded expenses and balances will be kept in order not to mess-up balances." +
                        "\n\nAre you sure you want to leave group?",
                )
            },
            icon = {
                Icon(
                    // TODO: Not 100% accurate icon
                    AdaptiveIcons.Outlined.ExitToApp,
                    contentDescription = "Leave group",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { onLeave() },
                ) {
                    Text(
                        text = "Yes, Leave",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        leaveDialogShown = false
                    },
                ) {
                    Text("No, Wait")
                }
            },
        )
    }

    LaunchedEffect(Unit) {
        tutorialControl.stepRequest(listOf(addParticipantTutorialStep))
    }
}

@Composable
private fun TopAppBareByState(
    dataState: GroupSettingsViewModel.DataState,
    onAction: (GroupSettingsAction) -> Unit,
    saveTutorialStep: TutorialStep,
    onToolbarAction: (GroupSettingTollbarAction) -> Unit,
) {
    val tutorialControl = LocalTutorialControl.current
    AdaptiveTopAppBar(
        title = {
            Text(
                stringResource(
                    when (dataState) {
                        GroupSettingsViewModel.DataState.Loading -> Res.string.loading
                        is GroupSettingsViewModel.DataState.Error -> Res.string.settings
                        is GroupSettingsViewModel.DataState.Group ->
                            if (dataState.id == null) {
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
            TutorialItem(
                onPositioned = { tutorialControl.onPositionRecieved(saveTutorialStep, it) },
            ) { modifier ->
                Box(
                    modifier =
                        modifier.fillMaxHeight(1f).clickable {
                            when (dataState) {
                                is GroupSettingsViewModel.DataState.Error ->
                                    onToolbarAction(
                                        GroupSettingTollbarAction.Reload,
                                    )

                                is GroupSettingsViewModel.DataState.Group ->
                                    onToolbarAction(
                                        GroupSettingTollbarAction.Commit,
                                    )

                                GroupSettingsViewModel.DataState.Loading -> {}
                            }
                        }.padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    when (dataState) {
                        is GroupSettingsViewModel.DataState.Error ->
                            Text(
                                // TODO: Add leading icon retry icon
                                text = stringResource(Res.string.retry),
                            )

                        is GroupSettingsViewModel.DataState.Group ->
                            if (dataState.id == null) {
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

                        GroupSettingsViewModel.DataState.Loading -> CircularProgressIndicator()
                    }
                }
            }
        },
    )
}
