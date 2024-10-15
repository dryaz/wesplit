package app.wesplit.participant

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.wesplit.domain.model.group.GroupRepository
import app.wesplit.domain.model.group.Participant
import app.wesplit.domain.model.group.isMe
import app.wesplit.domain.model.user.ContactListDelegate
import app.wesplit.domain.model.user.OnboardingStep
import app.wesplit.ui.HelpOverlayPosition
import app.wesplit.ui.TutorialControl
import app.wesplit.ui.TutorialItem
import app.wesplit.ui.TutorialOverlay
import app.wesplit.ui.TutorialStep
import io.github.alexzhirkevich.cupertino.adaptive.ExperimentalAdaptiveApi
import io.github.alexzhirkevich.cupertino.adaptive.icons.AccountBox
import io.github.alexzhirkevich.cupertino.adaptive.icons.AdaptiveIcons
import io.github.alexzhirkevich.cupertino.adaptive.icons.Delete
import io.github.alexzhirkevich.cupertino.adaptive.icons.Done
import io.github.alexzhirkevich.cupertino.adaptive.icons.Info
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
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
    val inputNameTutorial =
        remember {
            TutorialStep(
                title = "Type name, e.g. John",
                description = "You could invite real people later. Now just need to define who you need in group",
                onboardingStep = OnboardingStep.TYPE_PARTICIPANT_NAME,
                helpOverlayPosition = HelpOverlayPosition.BOTTOM_LEFT,
                isModal = false,
            )
        }

    val selectAndConfirmUser =
        remember {
            listOf(
                TutorialStep(
                    title = "Select new user's name",
                    description =
                        "Add this user by name. Invite them afterwards if needed. " +
                            "This user could already participate in expenses.",
                    onboardingStep = OnboardingStep.CREATE_NEW_USER_IN_GROUP,
                    helpOverlayPosition = HelpOverlayPosition.BOTTOM_RIGHT,
                    isModal = false,
                ),
                TutorialStep(
                    title = "Confirm your selection",
                    description =
                        "Don't worry, you could change participants anytime. " +
                            "Let's create group first.",
                    onboardingStep = OnboardingStep.APPLY_CHANGES,
                    helpOverlayPosition = HelpOverlayPosition.TOP_LEFT,
                    isModal = false,
                ),
            )
        }

    var needToHandleInputForTutorial by remember { mutableStateOf(true) }
    var showTutorial by remember { mutableStateOf(false) }
    var steps by remember { mutableStateOf<List<TutorialStep>>(emptyList()) }
    var currentStepIndex by remember { mutableStateOf(0) }
    val targetPositions = remember { mutableStateMapOf<TutorialStep, Rect>() }

    // TODO: remember?
    val tutorialControl =
        remember {
            TutorialControl(
                stepRequest = { requestedSteps ->
                    currentStepIndex = 0
                    println("Requested steps!!! : $requestedSteps")
                    steps = requestedSteps
                    showTutorial = true
                },
                onPositionRecieved = { step, rect ->
                    targetPositions[step] = rect
                },
                onNext = {
                    currentStepIndex++
                },
            )
        }

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

    val density = LocalDensity.current
    val tutorialPadding = with(density) { 54.dp.toPx() }

    ModalBottomSheet(
        modifier = Modifier.systemBarsPadding(),
        sheetState = sheetState,
        onDismissRequest = { onPickerClose() },
    ) {
        Box {
            Column {
                TutorialItem(
                    onPositioned = { rect ->
                        tutorialControl.onPositionRecieved(inputNameTutorial, rect)
                    },
                    isGlobalLayout = false,
                ) { modifier ->
                    OutlinedTextField(
                        modifier = modifier.fillMaxWidth(1f).padding(horizontal = 16.dp),
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
                }
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
                                newParticipantItem(
                                    state = state,
                                    currentParticipants = currentParticipants,
                                    onPositionReceived = { rect ->
                                        tutorialControl.onPositionRecieved(
                                            selectAndConfirmUser[0],
                                            rect.copy(
                                                top = rect.top + tutorialPadding,
                                                bottom = rect.bottom + tutorialPadding,
                                            ),
                                        )
                                    },
                                ) {
                                    tutorialControl.onNext()
                                    participantClickHandler(it)
                                    viewModel.searchText.update { "" }
                                }
                                currentParticipants(currentParticipants, participantClickHandler)
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

                    TutorialItem(
                        onPositioned = { rect ->
                            tutorialControl.onPositionRecieved(
                                selectAndConfirmUser[1],
                                rect.copy(
                                    top = rect.top + tutorialPadding,
                                    bottom = rect.bottom + tutorialPadding,
                                ),
                            )
                        },
                        isGlobalLayout = false,
                    ) { modifier ->
                        androidx.compose.animation.AnimatedVisibility(
                            modifier =
                                modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 8.dp)
                                    .widthIn(min = 120.dp),
                            visible = closeButtonVisibility,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            Button(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                onClick = {
                                    tutorialControl.onNext()
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
                    }
                } // TODO: Loading?
            }

            println("Is visible overlay?: ${showTutorial && currentStepIndex < steps.size}")
            androidx.compose.animation.AnimatedVisibility(
                visible = showTutorial && currentStepIndex < steps.size,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                val step = steps[minOf(currentStepIndex, steps.size - 1)]
                val targetRect = targetPositions[step]

                println("Step: $step")
                println("targetRect: $targetRect")

                TutorialOverlay(
                    targetBounds = targetRect,
                    step = step,
                    helpOverlayPosition = step.helpOverlayPosition,
                    onClose = { currentStepIndex++ },
                )
            }
        }
    }

    LaunchedEffect(sheetState) {
        snapshotFlow { sheetState.currentValue }
            .collect { value ->
                println("Value of sheet is $value")
                if (value == SheetValue.Expanded) {
                    tutorialControl.stepRequest(listOf(inputNameTutorial))
                }
            }
    }

    LaunchedEffect(searchText.value) {
        if (searchText.value.length > 3 && needToHandleInputForTutorial) {
            needToHandleInputForTutorial = false
            tutorialControl.stepRequest(selectAndConfirmUser)
        }
    }

    LaunchedEffect(currentStepIndex) {
        if (currentStepIndex >= steps.size - 1) {
            println("User completed $steps")
        }
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
            items(items = state.contacts.data, key = { it.id }) { participant ->
                ParticipantPickerItem(
                    participant = participant,
                    currentParticipants = currentParticipants,
                    onParticipantClick = onParticipantClick,
                )
                HorizontalDivider(modifier = Modifier.padding(start = 64.dp))
            }
        }
    }
}

private fun LazyListScope.currentParticipants(
    currentParticipants: Set<Participant>,
    onParticipantClick: (Participant) -> Unit,
) {
    if (currentParticipants.isNotEmpty()) {
        item {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).padding(top = 16.dp),
                text = "Current group participants",
                style = MaterialTheme.typography.titleSmall,
            )
        }

        items(items = currentParticipants.toList(), key = { it.id }) { participant ->
            ParticipantPickerItem(
                participant = participant,
                currentParticipants = currentParticipants,
                onParticipantClick = onParticipantClick,
                inGroupIcon = AdaptiveIcons.Outlined.Delete,
            )
            HorizontalDivider(modifier = Modifier.padding(start = 64.dp))
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

        items(items = state.connections, key = { it.id }) { participant ->
            ParticipantPickerItem(
                participant = participant,
                currentParticipants = currentParticipants,
                onParticipantClick = onParticipantClick,
            )
            HorizontalDivider(modifier = Modifier.padding(start = 64.dp))
        }
    }
}

private fun LazyListScope.newParticipantItem(
    state: ParticipantPickerViewModel.State.Suggestions,
    currentParticipants: Set<Participant>,
    onPositionReceived: (Rect) -> Unit,
    onParticipantClick: (Participant) -> Unit,
) {
    item {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).padding(top = 16.dp),
            text = stringResource(Res.string.create_new_contact),
            style = MaterialTheme.typography.titleSmall,
        )
        if (state.newParticipant != null) {
            TutorialItem(
                onPositioned = { onPositionReceived(it) },
                isGlobalLayout = false,
            ) { modifier ->
                ParticipantPickerItem(
                    modifier = modifier,
                    participant = state.newParticipant,
                    currentParticipants = currentParticipants,
                    onParticipantClick = onParticipantClick,
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxHeight(1f).padding(horizontal = 16.dp, vertical = 24.dp).padding(start = 16.dp),
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
    modifier: Modifier = Modifier,
    participant: Participant,
    currentParticipants: Set<Participant>,
    onParticipantClick: (Participant) -> Unit,
    inGroupIcon: ImageVector = AdaptiveIcons.Outlined.Done,
) {
    val clickHandler: ((Participant) -> Unit)? = if (participant.isMe()) null else onParticipantClick
    ParticipantListItem(
        modifier = if (participant.isMe()) modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh) else modifier,
        participant = participant,
        action = {
            if (participant in currentParticipants && !participant.isMe()) {
                Icon(
                    modifier = Modifier.minimumInteractiveComponentSize(),
                    imageVector = inGroupIcon,
                    contentDescription = stringResource(Res.string.user_already_in_group),
                )
            } else {
                Unit
            }
        },
        onClick = clickHandler,
    )
}
