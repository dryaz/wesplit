package app.wesplit.quicksplit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.FutureFeature
import app.wesplit.domain.model.currency.Amount
import app.wesplit.domain.model.currency.format
import app.wesplit.domain.model.group.Participant
import app.wesplit.participant.ParticipantListItem
import app.wesplit.participant.ParticipantPicker
import app.wesplit.quicksplit.QuickSplitViewModel.State.Data.ShareItem
import app.wesplit.ui.AdaptiveTopAppBar
import app.wesplit.ui.atoms.AmountField
import app.wesplit.ui.atoms.SwipeToDeleteItem
import app.wesplit.ui.molecules.CurrencyChooser
import app.wesplit.ui.molecules.ParticipantAvatars
import app.wesplit.ui.molecules.QuickAdd
import app.wesplit.ui.molecules.QuickAddAction
import app.wesplit.ui.molecules.QuickAddErrorState
import app.wesplit.ui.molecules.QuickAddState
import app.wesplit.ui.molecules.QuickAddValue
import io.github.alexzhirkevich.cupertino.adaptive.icons.AdaptiveIcons
import io.github.alexzhirkevich.cupertino.adaptive.icons.Add
import io.github.alexzhirkevich.cupertino.adaptive.icons.Clear
import io.github.alexzhirkevich.cupertino.adaptive.icons.Delete
import io.github.alexzhirkevich.cupertino.adaptive.icons.Edit
import io.github.alexzhirkevich.cupertino.adaptive.icons.KeyboardArrowDown
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.add_share
import split.composeapp.generated.resources.all_participants
import split.composeapp.generated.resources.delete_expense
import split.composeapp.generated.resources.edit_group
import split.composeapp.generated.resources.ic_down
import split.composeapp.generated.resources.ic_minus
import split.composeapp.generated.resources.loading
import split.composeapp.generated.resources.no_selected_user
import split.composeapp.generated.resources.quick_add_empty
import split.composeapp.generated.resources.quick_split
import split.composeapp.generated.resources.quick_split_total_participants
import split.composeapp.generated.resources.quick_split_turn
import split.composeapp.generated.resources.remove_share
import split.composeapp.generated.resources.select_payer_cd
import split.composeapp.generated.resources.settle_balances
import split.composeapp.generated.resources.undistributed

sealed interface QuickSplitAction {
    data object Back : QuickSplitAction

    data class Settle(
        val shares: Map<ShareItem, Map<Participant, Int>>,
    ) : QuickSplitAction
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun QuickSplitScreen(
    modifier: Modifier = Modifier,
    viewModel: QuickSplitViewModel,
    onAction: (QuickSplitAction) -> Unit,
) {
    val state = viewModel.state.collectAsState()
    val windowSizeClass = calculateWindowSizeClass()

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        topBar = {
            AdaptiveTopAppBar(
                actions = {
                    IconButton(
                        onClick = { viewModel.reset() },
                    ) {
                        Icon(
                            AdaptiveIcons.Outlined.Delete,
                            contentDescription = stringResource(Res.string.delete_expense),
                        )
                    }
                },
                title = {
                    Text(
                        when (state.value) {
                            is QuickSplitViewModel.State.Loading -> stringResource(Res.string.loading)
                            is QuickSplitViewModel.State.Data -> stringResource(Res.string.quick_split)
                        },
                    )
                },
                onNavigationIconClick =
                    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
                        { onAction(QuickSplitAction.Back) }
                    } else {
                        null
                    },
            )
        },
    ) { paddings ->
        when (val quickSplitState = state.value) {
            is QuickSplitViewModel.State.Data ->
                QuickSplitView(
                    modifier = Modifier.fillMaxSize(1f).padding(paddings),
                    onQuickSplitAction = onAction,
                    data = quickSplitState,
                ) { action ->
                    viewModel.update(action)
                }

            QuickSplitViewModel.State.Loading -> Text(stringResource(Res.string.loading))
        }
    }
}

@Composable
private fun QuickSplitView(
    modifier: Modifier = Modifier,
    data: QuickSplitViewModel.State.Data,
    onQuickSplitAction: (QuickSplitAction) -> Unit,
    onUpdated: (UpdateAction) -> Unit,
) {
    Column(
        modifier = modifier.padding(top = 16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ExpenseDetails(data, onUpdated)
        Spacer(modifier = Modifier.height(16.dp))
        SharesDetails(data, onQuickSplitAction, onUpdated)
        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
private fun SharesDetails(
    data: QuickSplitViewModel.State.Data,
    onQuickSplitAction: (QuickSplitAction) -> Unit,
    onUpdated: (UpdateAction) -> Unit,
) {
    Card(
        modifier = Modifier.widthIn(max = 450.dp).fillMaxWidth(1f).padding(horizontal = 16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            ),
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = stringResource(Res.string.quick_split_turn),
            style = MaterialTheme.typography.labelSmall,
        )

        var payerSelection by remember { mutableStateOf(false) }

        val headlineText =
            when {
                data.selectedParticipants.isEmpty() || data.participants.isEmpty() -> stringResource(Res.string.no_selected_user)
                else -> stringResource(Res.string.all_participants)
            }

        when {
            data.selectedParticipants.isEmpty() ||
                data.participants.isEmpty() ||
                data.selectedParticipants.size == data.participants.size ->
                ListItem(
                    modifier =
                        Modifier.clickable {
                            payerSelection = true
                        },
                    colors =
                        ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        ),
                    trailingContent = {
                        Icon(
                            AdaptiveIcons.Outlined.KeyboardArrowDown,
                            contentDescription = stringResource(Res.string.select_payer_cd),
                        )
                    },
                    headlineContent = {
                        Text(
                            text = headlineText,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    },
                    supportingContent =
                        if (data.selectedParticipants.isNotEmpty()) {
                            {
                                ParticipantAvatars(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    addIconEnabled = false,
                                    participants = data.selectedParticipants,
                                    size = 28.dp,
                                )
                            }
                        } else {
                            null
                        },
                )

            data.selectedParticipants.size == 1 ->
                ParticipantListItem(
                    modifier = Modifier,
                    addAlertEnabled = false,
                    participant = data.selectedParticipants.first(),
                    onClick = { payerSelection = true },
                    action = {
                        Icon(
                            painter = painterResource(Res.drawable.ic_down),
                            contentDescription = stringResource(Res.string.select_payer_cd),
                        )
                    },
                )
        }

        var quickAddData: QuickAddValue by remember { mutableStateOf(QuickAddValue()) }
        var quickAddError: QuickAddErrorState by remember { mutableStateOf(QuickAddErrorState.NONE) }

        val quickAddCommitCallback = {
            if (quickAddData.title.isNullOrBlank()) {
                quickAddError = QuickAddErrorState.TITLE
            } else if ((quickAddData.amount ?: 0.0) == 0.0) {
                quickAddError = QuickAddErrorState.AMOUNT
            } else {
                quickAddData.amount?.let { amount ->
                    onUpdated(
                        UpdateAction.AddItem(
                            QuickSplitViewModel.State.Data.ShareItem(
                                title = quickAddData.title,
                                priceValue = amount,
                            ),
                            participants = data.selectedParticipants,
                        ),
                    )
                    quickAddError = QuickAddErrorState.NONE
                    quickAddData = QuickAddValue()
                }
            }
        }

        QuickAdd(
            modifier = Modifier.padding(horizontal = 16.dp),
            state =
                QuickAddState.Data(
                    value = quickAddData,
                    error = quickAddError,
                ),
            textFieldColors =
                TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            showCurrency = false,
            onAction = { action ->
                when (action) {
                    is QuickAddAction.Change -> quickAddData = action.value ?: QuickAddValue()
                    QuickAddAction.Commit -> quickAddCommitCallback()
                    QuickAddAction.RequestPaywall -> TODO("Quick add should not be hidden in QuickSplit")
                }
            },
        )

        Spacer(modifier = Modifier.height(8.dp))

        data.items.mapValues { (item, participants) ->
            ShareListItem(
                participants = participants,
                item = item,
                currencyCode = data.amount.currencyCode,
                onUpdated = onUpdated,
                onPlus = {
                    onUpdated(
                        UpdateAction.UpdateShareParticipants(
                            share = item,
                            participants = data.selectedParticipants,
                            shareDx = 1,
                        ),
                    )
                },
                onMinus = {
                    onUpdated(
                        UpdateAction.UpdateShareParticipants(
                            share = item,
                            participants = data.selectedParticipants,
                            shareDx = -1,
                        ),
                    )
                },
            )
        }

        if (data.items.isEmpty() && data.undistributedValue == 0.0) {
            Text(
                modifier = Modifier.fillMaxWidth(1f).padding(top = 8.dp, bottom = 16.dp),
                text = stringResource(Res.string.quick_add_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center,
            )
        }

        AnimatedVisibility(
            visible = data.undistributedValue != 0.0,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Column {
                HorizontalDivider()
                Row(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(
                        modifier = Modifier,
                        text = stringResource(Res.string.undistributed),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${Amount(value = data.undistributedValue, currencyCode = data.amount.currencyCode).format()}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = data.items.isNotEmpty(),
        ) {
            ListItem(
                modifier =
                    Modifier.fillMaxWidth().then(
                        if (data.undistributedValue == 0.0) {
                            Modifier.clickable {
                                onQuickSplitAction(QuickSplitAction.Settle(data.items))
                            }
                        } else {
                            Modifier
                        },
                    ),
                colors =
                    ListItemDefaults.colors(
                        containerColor =
                            if (data.undistributedValue == 0.0) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            },
                        headlineColor =
                            if (data.undistributedValue == 0.0) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                    ),
                headlineContent = {
                    Text(
                        modifier = Modifier.fillMaxSize(1f),
                        text = stringResource(Res.string.settle_balances),
                        textAlign = TextAlign.Center,
                    )
                },
            )
        }

        PayerChooser(
            expanded = payerSelection,
            allParticipants = data.participants,
            onDismiss = { payerSelection = false },
            onUpdated = onUpdated,
        )
    }
}

@Composable
private fun ShareListItem(
    participants: Map<Participant, Int>,
    currencyCode: String,
    item: QuickSplitViewModel.State.Data.ShareItem,
    onUpdated: (UpdateAction) -> Unit,
    onPlus: () -> Unit,
    onMinus: () -> Unit,
) {
    SwipeToDeleteItem(
        onDelete = {
            onUpdated(UpdateAction.RemoveItem(item))
        },
    ) {
        ListItem(
            modifier = Modifier,
            colors =
                ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
            trailingContent = {
                Column(
                    horizontalAlignment = Alignment.End,
                ) {
                    Text(
                        text = "${Amount(value = item.priceValue, currencyCode = currencyCode).format()}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        FilledTonalButton(
                            onClick = onMinus,
                            shape =
                                RoundedCornerShape(
                                    topStart = 5.dp,
                                    bottomStart = 5.dp,
                                ),
                        ) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.ic_minus),
                                contentDescription = stringResource(Res.string.remove_share),
                            )
                        }
                        FilledTonalButton(
                            onClick = onPlus,
                            shape =
                                RoundedCornerShape(
                                    topEnd = 5.dp,
                                    bottomEnd = 5.dp,
                                ),
                        ) {
                            Icon(
                                AdaptiveIcons.Outlined.Add,
                                contentDescription = stringResource(Res.string.add_share),
                            )
                        }
                    }
                }
            },
            headlineContent = {
                Column {
                    Text(
                        text = "${item.title}",
                    )
                    ParticipantAvatars(
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                        // TODO: show participants shares
                        participants =
                            participants.flatMap { (participant, count) ->
                                List(count) { participant }
                            },
                        addIconEnabled = false,
                        size = 28.dp,
                    )
                }
            },
        )
    }
}

@Composable
private fun ExpenseDetails(
    data: QuickSplitViewModel.State.Data,
    onUpdated: (UpdateAction) -> Unit,
) {
    var userSelectorVisibility by rememberSaveable { mutableStateOf(false) }

    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            ),
        modifier = Modifier.widthIn(max = 450.dp).fillMaxWidth(1f).padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.height(IntrinsicSize.Max).fillMaxWidth(1f).padding(horizontal = 16.dp),
        ) {
            CurrencyChooser(
                modifier = Modifier.fillMaxHeight(1f),
                selectedCurrencyCode = data.amount.currencyCode,
            ) { newCurrency ->
                onUpdated(UpdateAction.UpdateAmountCurrency(newCurrency))
            }
            Spacer(modifier = Modifier.width(8.dp))
            AmountField(
                modifier = Modifier.fillMaxWidth(1f),
                value = data.amount.value,
            ) { newAmount ->
                onUpdated(UpdateAction.UpdateAmountValue(newAmount))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ListItem(
            modifier =
                Modifier.clickable {
                    userSelectorVisibility = true
                },
            colors =
                ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
            trailingContent = {
                Icon(
                    AdaptiveIcons.Outlined.Edit,
                    contentDescription = stringResource(Res.string.edit_group),
                )
            },
            headlineContent = {
                Text(
                    text = stringResource(Res.string.quick_split_total_participants, data.participants.size),
                    color = MaterialTheme.colorScheme.outline,
                )
            },
            supportingContent = {
                ParticipantAvatars(
                    modifier = Modifier.padding(vertical = 8.dp),
                    addIconEnabled = false,
                    participants = data.participants,
                )
            },
        )
    }

    AnimatedVisibility(visible = userSelectorVisibility) {
        val callback: (Participant) -> Unit =
            remember(data.participants) {
                { user ->
                    val newParticipants =
                        if (user in data.participants) {
                            data.participants - user
                        } else {
                            data.participants + user
                        }
                    onUpdated(UpdateAction.UpdateExpenseParticipants(newParticipants))
                }
            }
        ParticipantPicker(
            currentParticipants = data.participants,
            isFullScreen = true,
            onPickerClose = {
                userSelectorVisibility = false
            },
            onParticipantClick = callback,
        )
    }
}

// TODO: to Modal sheet, add option to choose 2+ ppl
@FutureFeature
@Composable
private fun PayerChooser(
    expanded: Boolean,
    allParticipants: Set<Participant>,
    onDismiss: () -> Unit,
    onUpdated: (UpdateAction) -> Unit,
) {
    DropdownMenu(
        modifier = Modifier.requiredSizeIn(maxHeight = 360.dp, minWidth = 360.dp),
        expanded = expanded,
        onDismissRequest = { onDismiss() },
    ) {
        DropdownMenuItem(
            text = {
                ListItem(
                    colors =
                        ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        ),
                    headlineContent = {
                        Text(
                            text = stringResource(Res.string.no_selected_user),
                            color = MaterialTheme.colorScheme.outline,
                        )
                    },
                    leadingContent = {
                        Icon(
                            AdaptiveIcons.Outlined.Clear,
                            contentDescription = stringResource(Res.string.no_selected_user),
                        )
                    },
                )
            },
            onClick = {
                onDismiss()
                onUpdated(UpdateAction.UpdateSelectedParticipants(emptySet()))
            },
        )

        DropdownMenuItem(
            text = {
                ListItem(
                    colors =
                        ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        ),
                    headlineContent = {
                        Text(
                            text = stringResource(Res.string.all_participants),
                            color = MaterialTheme.colorScheme.outline,
                        )
                    },
                    supportingContent = {
                        ParticipantAvatars(
                            modifier = Modifier.padding(vertical = 8.dp),
                            participants = allParticipants,
                            addIconEnabled = false,
                            size = 28.dp,
                        )
                    },
                )
            },
            onClick = {
                onDismiss()
                onUpdated(UpdateAction.UpdateSelectedParticipants(allParticipants))
            },
        )

        HorizontalDivider()

        allParticipants.forEach { participant ->
            DropdownMenuItem(
                text = {
                    ParticipantListItem(
                        participant = participant,
                        addAlertEnabled = false,
                    )
                },
                onClick = {
                    onDismiss()
                    onUpdated(UpdateAction.UpdateSelectedParticipants(setOf(participant)))
                },
            )
        }
    }
}
