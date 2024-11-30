package app.wesplit.group.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.account.Account
import app.wesplit.domain.model.group.Participant
import app.wesplit.domain.model.group.isMe
import app.wesplit.domain.model.user.OnboardingStep
import app.wesplit.group.GroupImage
import app.wesplit.participant.ParticipantListItem
import app.wesplit.participant.ParticipantPicker
import app.wesplit.ui.AdaptiveTopAppBar
import app.wesplit.ui.tutorial.HelpOverlayPosition
import app.wesplit.ui.tutorial.LocalTutorialControl
import app.wesplit.ui.tutorial.TutorialControl
import app.wesplit.ui.tutorial.TutorialItem
import app.wesplit.ui.tutorial.TutorialStep
import io.github.alexzhirkevich.cupertino.adaptive.icons.AdaptiveIcons
import io.github.alexzhirkevich.cupertino.adaptive.icons.Delete
import io.github.alexzhirkevich.cupertino.adaptive.icons.ExitToApp
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.add_group_image
import split.composeapp.generated.resources.add_user_to_group
import split.composeapp.generated.resources.confirm_no_wait
import split.composeapp.generated.resources.confirm_yes_join
import split.composeapp.generated.resources.confirm_yes_leave
import split.composeapp.generated.resources.create
import split.composeapp.generated.resources.error
import split.composeapp.generated.resources.forget_group
import split.composeapp.generated.resources.group_name
import split.composeapp.generated.resources.ic_add_image
import split.composeapp.generated.resources.ic_user_add
import split.composeapp.generated.resources.image_description_ai
import split.composeapp.generated.resources.join_group
import split.composeapp.generated.resources.join_group_as_new_participant
import split.composeapp.generated.resources.join_group_as_user
import split.composeapp.generated.resources.join_new_participant
import split.composeapp.generated.resources.leave_group
import split.composeapp.generated.resources.leave_group_confirmation
import split.composeapp.generated.resources.loading
import split.composeapp.generated.resources.new_group
import split.composeapp.generated.resources.retry
import split.composeapp.generated.resources.save
import split.composeapp.generated.resources.settings
import split.composeapp.generated.resources.text_its_me
import split.composeapp.generated.resources.title_leave_group
import split.composeapp.generated.resources.tutorial_step_add_participant_description
import split.composeapp.generated.resources.tutorial_step_add_participant_title

private const val IMAGE_CLICK = "image_click_settings"

sealed interface GroupSettingsAction {
    data object Back : GroupSettingsAction

    data object Home : GroupSettingsAction
}

private sealed interface GroupSettingTollbarAction {
    data object Reload : GroupSettingTollbarAction

    data object Commit : GroupSettingTollbarAction
}

private val addParticipantTutorialStep =
    TutorialStep(
        title = Res.string.tutorial_step_add_participant_title,
        description = Res.string.tutorial_step_add_participant_description,
        onboardingStep = OnboardingStep.ADD_NEW_USER_BUTTON,
        isModal = false,
        helpOverlayPosition = HelpOverlayPosition.BOTTOM_LEFT,
    )

@Composable
fun GroupSettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: GroupSettingsViewModel,
    onAction: (GroupSettingsAction) -> Unit,
) {
    val state = viewModel.state.collectAsState()
    val tutorialControl = LocalTutorialControl.current
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        topBar = {
            TopAppBareByState(
                dataState = state.value.dataState,
                onAction = onAction,
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
            is GroupSettingsViewModel.DataState.Error -> Text(stringResource(Res.string.error))
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
                        onAction(GroupSettingsAction.Back)
                    },
                    tutorialControl = tutorialControl,
                ) { group ->
                    viewModel.update(group)
                }
            // TODO: Shimmer?
            GroupSettingsViewModel.DataState.Loading -> Text(stringResource(Res.string.loading))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.event.collectLatest { event ->
            when (event) {
                is GroupSettingsViewModel.Event.Error -> {
                    snackbarHostState.showSnackbar(event.msg)
                }
            }
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
    onDone: () -> Unit,
    onJoin: (Participant?) -> Unit,
    onLeave: () -> Unit,
    onUpdated: (GroupSettingsViewModel.DataState.Group) -> Unit,
) {
    val analyticsManager: AnalyticsManager = koinInject()
    val focusRequester: FocusRequester = remember { FocusRequester() }
    var userSelectorVisibility by rememberSaveable { mutableStateOf(false) }
    var leaveDialogShown by remember { mutableStateOf(false) }
    var joinAsDialogShown by remember { mutableStateOf<Set<Participant>?>(null) }

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
                GroupImage(
                    modifier = Modifier.padding(top = 8.dp),
                    imageUrl = group.imageUrl,
                    groupTitle = group.title,
                    placeholder = {
                        Box(
                            modifier =
                                Modifier.size(52.dp).clip(RoundedCornerShape(15.dp))
                                    .border(
                                        width = 2.dp,
                                        color = MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(15.dp),
                                    ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                painter = painterResource(Res.drawable.ic_add_image),
                                contentDescription = stringResource(Res.string.add_group_image),
                            )
                        }
                    },
                ) {
                    analyticsManager.track(IMAGE_CLICK)
                    focusRequester.requestFocus()
                }

                Spacer(modifier = Modifier.width(16.dp))
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
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

            OutlinedTextField(
                modifier =
                    Modifier
                        .fillMaxWidth(1f)
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                        .focusRequester(focusRequester),
                value = group.imageDescription ?: "",
                onValueChange = { onUpdated(group.copy(imageDescription = it)) },
                label = {
                    Text(stringResource(Res.string.image_description_ai))
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
                                    onClick = { joinAsDialogShown = setOf(participant) },
                                ) {
                                    Text(stringResource(Res.string.text_its_me))
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
                    onClick = { joinAsDialogShown = emptySet() },
                    modifier =
                        Modifier.widthIn(max = 450.dp)
                            .fillMaxWidth(1f)
                            .padding(horizontal = 16.dp),
                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                ) {
                    Text(stringResource(Res.string.join_new_participant))
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
                    if (isMeParticipating) stringResource(Res.string.leave_group) else stringResource(Res.string.forget_group),
                )
            }
        }
    }

    if (joinAsDialogShown != null) {
        AlertDialog(
            modifier = Modifier.widthIn(max = 450.dp),
            onDismissRequest = { joinAsDialogShown = null },
            title = { Text(stringResource(Res.string.join_group)) },
            text = {
                Text(
                    text =
                        if (joinAsDialogShown.isNullOrEmpty()) {
                            stringResource(Res.string.join_group_as_new_participant, group.title)
                        } else {
                            stringResource(Res.string.join_group_as_user, group.title, "${joinAsDialogShown?.first()?.name}")
                        },
                    textAlign = TextAlign.Center,
                )
            },
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_user_add),
                    contentDescription = stringResource(Res.string.join_group),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onJoin(joinAsDialogShown?.firstOrNull())
                        joinAsDialogShown = null
                    },
                ) {
                    Text(
                        text = stringResource(Res.string.confirm_yes_join),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        joinAsDialogShown = null
                    },
                ) {
                    Text(
                        text = stringResource(Res.string.confirm_no_wait),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            },
        )
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
                    onUpdated(group.copy(participants = newParticipants))
                }
            }
        ParticipantPicker(
            currentParticipants = group.participants,
            isFullScreen = true,
            onPickerClose = {
                userSelectorVisibility = false
            },
            onParticipantClick = callback,
        )
    }

    if (leaveDialogShown) {
        AlertDialog(
            modifier = Modifier.widthIn(max = 450.dp),
            onDismissRequest = { leaveDialogShown = false },
            title = { Text(stringResource(Res.string.title_leave_group, group.title)) },
            text = { Text(stringResource(Res.string.leave_group_confirmation)) },
            icon = {
                Icon(
                    // TODO: Not 100% accurate icon
                    AdaptiveIcons.Outlined.ExitToApp,
                    contentDescription = stringResource(Res.string.leave_group),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { onLeave() },
                ) {
                    Text(
                        text = stringResource(Res.string.confirm_yes_leave),
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
                    Text(stringResource(Res.string.confirm_no_wait))
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
    onToolbarAction: (GroupSettingTollbarAction) -> Unit,
) {
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
            Box(
                modifier =
                    Modifier.fillMaxHeight(1f).clickable {
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
        },
    )
}
