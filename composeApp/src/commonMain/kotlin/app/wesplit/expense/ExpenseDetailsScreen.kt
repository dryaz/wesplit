package app.wesplit.expense

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import app.wesplit.currency.CurrencyPicker
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.FutureFeature
import app.wesplit.domain.model.account.Account
import app.wesplit.domain.model.currency.currencySymbol
import app.wesplit.domain.model.currency.format
import app.wesplit.domain.model.expense.SplitType
import app.wesplit.domain.model.expense.allowedToChange
import app.wesplit.domain.model.expense.isProtected
import app.wesplit.domain.model.expense.toInstant
import app.wesplit.domain.model.group.Participant
import app.wesplit.domain.model.group.uiTitle
import app.wesplit.domain.model.user.OnboardingStep
import app.wesplit.expense.ExpenseDetailsViewModel.State.Loading.allParticipants
import app.wesplit.filterDoubleInput
import app.wesplit.participant.ParticipantListItem
import app.wesplit.ui.AdaptiveTopAppBar
import app.wesplit.ui.PlusProtected
import app.wesplit.ui.tutorial.HelpOverlayPosition
import app.wesplit.ui.tutorial.LocalTutorialControl
import app.wesplit.ui.tutorial.TutorialItem
import app.wesplit.ui.tutorial.TutorialStep
import io.github.alexzhirkevich.cupertino.adaptive.ExperimentalAdaptiveApi
import io.github.alexzhirkevich.cupertino.adaptive.icons.AdaptiveIcons
import io.github.alexzhirkevich.cupertino.adaptive.icons.Create
import io.github.alexzhirkevich.cupertino.adaptive.icons.Delete
import io.github.alexzhirkevich.cupertino.adaptive.icons.Done
import io.github.alexzhirkevich.cupertino.adaptive.icons.Lock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.add_expense_to_group
import split.composeapp.generated.resources.create
import split.composeapp.generated.resources.ic_down
import split.composeapp.generated.resources.ic_flag
import split.composeapp.generated.resources.loading
import split.composeapp.generated.resources.new_expense
import split.composeapp.generated.resources.retry
import split.composeapp.generated.resources.save
import split.composeapp.generated.resources.select_payer_cd
import split.composeapp.generated.resources.settings
import kotlin.math.roundToInt

private const val CHANGE_SPLIT_TYPE_EVENT = "exp_change_split_type"
private const val CHANGE_SPLIT_TYPE_PARAM = "type"

private const val COMMIT_INVALID = "try_commit_not_complete"

sealed interface AddExpenseAction {
    data object Back : AddExpenseAction
}

private sealed interface AddExpenseTollbarAction {
    data object Commit : AddExpenseTollbarAction
}

internal val checkBalanceTutorialStepFlow =
    listOf(
        TutorialStep(
            title = "Enter expense title",
            description = "Type smth that defines this expense. Let's type Launch for example.",
            onboardingStep = OnboardingStep.EXPENSE_TITLE,
            isModal = false,
            helpOverlayPosition = HelpOverlayPosition.BOTTOM_LEFT,
        ),
        TutorialStep(
            title = "Date and currency setup",
            description =
                "You could adjust date of expense or currency. By default date is today and currency is the one last used.",
            onboardingStep = OnboardingStep.EXPENSE_DATE_CURRENCY,
            isModal = true,
            helpOverlayPosition = HelpOverlayPosition.BOTTOM_LEFT,
        ),
        TutorialStep(
            title = "Put the amount",
            description = "What was the total bill of this expense, put it here.",
            onboardingStep = OnboardingStep.EXPENSE_AMOUNT,
            isModal = false,
            helpOverlayPosition = HelpOverlayPosition.BOTTOM_RIGHT,
        ),
        TutorialStep(
            title = "Choose the payer",
            description = "By default you're the payer. But you could choose from all group participants.",
            onboardingStep = OnboardingStep.EXPENSE_PAYER,
            isModal = true,
            helpOverlayPosition = HelpOverlayPosition.TOP_LEFT,
        ),
        TutorialStep(
            title = "Define each person's cut",
            description =
                "By default expense is equally shared among participants. " +
                    "You could define each sum by participation, shares or exact amount.",
            onboardingStep = OnboardingStep.EXPENSE_SPLIT,
            isModal = true,
            helpOverlayPosition = HelpOverlayPosition.TOP_RIGHT,
        ),
        TutorialStep(
            title = "Save the expense",
            description = "When everything is done - just save it. No worries - you could edit or delete it anytime.",
            onboardingStep = OnboardingStep.EXPENSE_SAVE,
            isModal = false,
            helpOverlayPosition = HelpOverlayPosition.TOP_LEFT,
        ),
    )

private fun getStep(type: OnboardingStep) = checkBalanceTutorialStepFlow.first { it.onboardingStep == type }

// TODO: Clear hardcoded strings
@Composable
fun ExpenseDetailsScreen(
    modifier: Modifier = Modifier,
    viewModel: ExpenseDetailsViewModel,
    onAction: (AddExpenseAction) -> Unit,
) {
    val state = viewModel.state.collectAsState()
    val analyticsManager: AnalyticsManager = koinInject()
    val tutorialControl = LocalTutorialControl.current

    fun commit() {
        (state.value as? ExpenseDetailsViewModel.State.Data)?.let {
            if (it.isComplete) {
                viewModel.update(UpdateAction.Commit)
                onAction(AddExpenseAction.Back)
            } else {
                analyticsManager.track(COMMIT_INVALID)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        floatingActionButton = {
            TutorialItem(
                onPositioned = { tutorialControl.onPositionRecieved(getStep(OnboardingStep.EXPENSE_SAVE), it) },
            ) { modifier ->
                FloatingActionButton(
                    modifier = modifier,
                    onClick = {
                        tutorialControl.onNext()
                        commit()
                        // TODO: Show error in case of it not yet ready?
                    },
                ) {
                    Icon(
                        AdaptiveIcons.Outlined.Done,
                        contentDescription = stringResource(Res.string.add_expense_to_group),
                    )
                }
            }
        },
        topBar = {
            TopAppBareByState(
                state = state.value,
                onAction = onAction,
                onToolbarAction = { action ->
                    when (action) {
                        AddExpenseTollbarAction.Commit -> commit()
                    }
                },
            )
        },
    ) { paddings ->
        when (val expenseState = state.value) {
            is ExpenseDetailsViewModel.State.Error -> Text("Error")
            is ExpenseDetailsViewModel.State.Data ->
                AddExpenseScreenView(
                    modifier = Modifier.fillMaxSize(1f).padding(paddings),
                    data = expenseState,
                ) { action ->
                    viewModel.update(action)
                    if (action == UpdateAction.Delete || action == UpdateAction.Commit) {
                        onAction(AddExpenseAction.Back)
                    }
                }
            // TODO: Shimmer?
            ExpenseDetailsViewModel.State.Loading -> Text("Loading")
        }
    }

    LaunchedEffect(Unit) {
        tutorialControl.stepRequest(checkBalanceTutorialStepFlow)
    }
}

@Composable
private fun AddExpenseScreenView(
    modifier: Modifier = Modifier,
    data: ExpenseDetailsViewModel.State.Data,
    onUpdated: (UpdateAction) -> Unit,
) {
    var deleteDialogShown by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.padding(top = 16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ExpenseDetails(data, onUpdated)
        Spacer(modifier = Modifier.height(16.dp))
        SharesDetails(data, onUpdated)

        if (data.expense.id != null) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                // TODO: Confirmation
                onClick = { deleteDialogShown = true },
                enabled = data.expense.allowedToChange(),
                modifier = Modifier.widthIn(max = 450.dp).fillMaxWidth(1f).padding(horizontal = 16.dp),
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
            ) {
                Text("Delete expense")
            }
        }
        Spacer(modifier = Modifier.height(64.dp))
    }

    if (deleteDialogShown) {
        AlertDialog(
            modifier = Modifier.widthIn(max = 450.dp),
            onDismissRequest = { deleteDialogShown = false },
            title = { Text("Delete ${data.expense.title}?") },
            text = { Text("Are you sure you want to delete this expense from '${data.group.title}'?") },
            icon = {
                Icon(
                    AdaptiveIcons.Outlined.Delete,
                    contentDescription = "Delete expense from group",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { onUpdated(UpdateAction.Delete) },
                ) {
                    Text(
                        text = "Yes, Delete",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        deleteDialogShown = false
                    },
                ) {
                    Text("No, Wait")
                }
            },
        )
    }
}

@OptIn(ExperimentalAdaptiveApi::class)
@Composable
private fun ProtectionBlock(
    onUpdated: (UpdateAction) -> Unit,
    data: ExpenseDetailsViewModel.State.Data,
) {
    val protectCallback: (Boolean) -> Unit =
        remember {
            { onUpdated(UpdateAction.Protect(it)) }
        }
    val clickabeModifier =
        if (data.expense.allowedToChange() && data.account is Account.Authorized) {
            Modifier.clickable { protectCallback(!data.expense.isProtected()) }
        } else {
            Modifier
        }

    ListItem(
        modifier = clickabeModifier,
        leadingContent = {
            Icon(
                modifier = Modifier.minimumInteractiveComponentSize(),
                imageVector = AdaptiveIcons.Outlined.Lock,
                contentDescription = "Protect expense from editing",
            )
        },
        headlineContent = {
            PlusProtected {
                Text(
                    text =
                        if (data.expense.allowedToChange()) {
                            "Protect expense"
                        } else {
                            "Protected expense"
                        },
                )
            }
        },
        supportingContent = {
            Text(
                text =
                    if (data.expense.allowedToChange()) {
                        if (data.account is Account.Authorized) {
                            "Only you able to update this expense"
                        } else {
                            "Login to protect this expense"
                        }
                    } else {
                        "You're not allowed to update this expense"
                    },
            )
        },
        colors =
            ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            ),
        trailingContent = {
            if (data.expense.allowedToChange() && data.account is Account.Authorized) {
                Switch(
                    checked = data.expense.isProtected(),
                    enabled = data.expense.allowedToChange(),
                    onCheckedChange = { protectCallback(it) },
                )
            }
        },
    )
}

@Composable
private fun SharesDetails(
    data: ExpenseDetailsViewModel.State.Data,
    onUpdated: (UpdateAction) -> Unit,
) {
    val tutorialControl = LocalTutorialControl.current
    TutorialItem(
        onPositioned = { tutorialControl.onPositionRecieved(getStep(OnboardingStep.EXPENSE_SPLIT), it) },
    ) { modifier ->
        Card(
            modifier = modifier.widthIn(max = 450.dp).fillMaxWidth(1f).padding(horizontal = 16.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
        ) {
            SharesDetailsHeader(data)
            SharesDetailsParticipants(data, data.splitOptions.selectedSplitType, onUpdated)
        }
    }
}

@Composable
fun SharesDetailsParticipants(
    data: ExpenseDetailsViewModel.State.Data,
    splitType: SplitType,
    onUpdated: (UpdateAction) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val analyticsManager: AnalyticsManager = koinInject()
    var selectedTabIndex by remember {
        mutableIntStateOf(
            when (splitType) {
                SplitType.EQUAL -> 0
                SplitType.SHARES -> 1
                SplitType.AMOUNTS -> 2
            },
        )
    }

    Column {
        TabRow(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            selectedTabIndex = selectedTabIndex,
        ) {
            Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }, text = { Text("Equal") })
            Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }, text = { Text("Shares") })
            Tab(selected = selectedTabIndex == 2, onClick = { selectedTabIndex = 2 }, text = { Text("Amounts") })
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(1f),
        ) { index ->
            Column(
                modifier = Modifier.fillMaxSize(1f),
            ) {
                when (index) {
                    0 -> SharesDetailsParticipantList(data, SplitType.EQUAL, onUpdated)
                    1 -> SharesDetailsParticipantList(data, SplitType.SHARES, onUpdated)
                    2 -> SharesDetailsParticipantList(data, SplitType.AMOUNTS, onUpdated)
                }
            }
        }
    }

    LaunchedEffect(key1 = selectedTabIndex) {
        pagerState.animateScrollToPage(selectedTabIndex)
        val type =
            when (selectedTabIndex) {
                0 -> SplitType.EQUAL
                1 -> SplitType.SHARES
                2 -> SplitType.AMOUNTS
                else -> null
            }
        type?.let {
            analyticsManager.track(
                CHANGE_SPLIT_TYPE_EVENT,
                mapOf(
                    CHANGE_SPLIT_TYPE_PARAM to type.name,
                ),
            )
        }
    }

    LaunchedEffect(key1 = pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            selectedTabIndex = pagerState.currentPage
        }
    }
}

@Composable
private fun SharesDetailsParticipantList(
    data: ExpenseDetailsViewModel.State.Data,
    splitType: SplitType,
    onUpdated: (UpdateAction) -> Unit,
) {
    data.allParticipants().forEach { participant ->
        when (splitType) {
            SplitType.EQUAL -> EqualSplit(data, splitType, participant, onUpdated)

            SplitType.SHARES ->
                AmountsSplit(participant, data, splitType) { person, value ->
                    onUpdated(
                        UpdateAction.Split.Share(
                            participant = person,
                            value = value,
                        ),
                    )
                }

            SplitType.AMOUNTS ->
                AmountsSplit(participant, data, splitType) { person, value ->
                    onUpdated(
                        UpdateAction.Split.Amount(
                            participant = person,
                            value = value,
                        ),
                    )
                }
        }
    }

    with(data.expense.undistributedAmount) {
        val undist = this
        AnimatedVisibility(visible = undist != null && undist.value != 0.0) {
            ListItem(
                colors =
                    ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    ),
                headlineContent = {
                    Text(
                        text = "Undistributed",
                    )
                },
                leadingContent = {
                    Icon(
                        modifier = Modifier.width(56.dp),
                        painter = painterResource(Res.drawable.ic_flag),
                        contentDescription = "Undistributed",
                    )
                },
                trailingContent = {
                    undist?.let { amount ->
                        FilterChip(
                            selected = false,
                            onClick = {},
                            enabled = false,
                            label = { Text(amount.format()) },
                            colors =
                                FilterChipDefaults.filterChipColors(
                                    disabledContainerColor = MaterialTheme.colorScheme.error,
                                    disabledLabelColor = MaterialTheme.colorScheme.onError,
                                ),
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun AmountsSplit(
    participant: Participant,
    data: ExpenseDetailsViewModel.State.Data,
    splitType: SplitType,
    onUpdated: (Participant, Double) -> Unit,
) {
    ParticipantListItem(
        participant = participant,
        action = {
            val focusRequester = remember { FocusRequester() }
            var fieldValue by remember(data.splitOptions) {
                val splitValue = (data.splitOptions.splitValues[splitType]!![participant] ?: 0.0) as Double
                val roundedSplitValue = (splitValue * 100.0).roundToInt() / 100.0
                val splitValueStr = roundedSplitValue.toString()
                val strValue = if (splitValueStr.endsWith(".0")) splitValueStr.dropLast(2) else splitValueStr
                mutableStateOf(
                    TextFieldValue(
                        text = strValue,
                        selection = TextRange(strValue.length),
                    ),
                )
            }
            TextField(
                modifier =
                    Modifier.width(90.dp).focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                fieldValue =
                                    fieldValue.copy(
                                        selection = TextRange(0, fieldValue.text.length),
                                    )
                            } else {
                                fieldValue =
                                    fieldValue.copy(
                                        selection = TextRange(fieldValue.text.length, fieldValue.text.length),
                                    )
                            }
                        },
                singleLine = true,
                enabled = data.expense.allowedToChange(),
                value = fieldValue,
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                    ),
                onValueChange = { newValue ->
                    val newFilteredValue = newValue.text.filterDoubleInput()
                    val isEndingWithDot = newFilteredValue.endsWith(".")
                    var needUpdate = false

                    if (!isEndingWithDot) {
                        needUpdate = true
                    }
                    fieldValue = newValue.copy(text = newFilteredValue)

                    if (needUpdate) {
                        val doubleValue = newFilteredValue.toDoubleOrNull() ?: 0.0
                        onUpdated(participant, doubleValue)
                    }
                },
            )
        },
        subComposable = {
            Text(
                text =
                    data.expense.shares.find { it.participant.id == participant.id }?.let {
                        it.amount.format()
                    } ?: "Not participating",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )
        },
    )
}

@Composable
private fun EqualSplit(
    data: ExpenseDetailsViewModel.State.Data,
    splitType: SplitType,
    participant: Participant,
    onUpdated: (UpdateAction) -> Unit,
) {
    val isParticipating = data.splitOptions.splitValues[splitType]!![participant] as Boolean
    ParticipantListItem(
        participant = participant,
        enabled = data.expense.allowedToChange(),
        onClick = { item ->
            onUpdated(
                UpdateAction.Split.Equal(
                    participant = item,
                    value = !isParticipating,
                ),
            )
        },
        action = {
            Checkbox(
                checked = isParticipating,
                enabled = data.expense.allowedToChange(),
                onCheckedChange = { isChecked ->
                    onUpdated(
                        UpdateAction.Split.Equal(
                            participant = participant,
                            value = isChecked,
                        ),
                    )
                },
            )
        },
        subComposable = {
            Text(
                text =
                    data.expense.shares.find { it.participant.id == participant.id }?.let {
                        it.amount.format()
                    } ?: "Not participating",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )
        },
    )
}

@Composable
private fun SharesDetailsHeader(data: ExpenseDetailsViewModel.State.Data) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "We split in",
            modifier = Modifier.padding(vertical = 16.dp).padding(start = 16.dp),
            color = MaterialTheme.colorScheme.outline,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = data.group.uiTitle(),
            modifier = Modifier.padding(vertical = 16.dp).padding(end = 16.dp),
        )
    }
    HorizontalDivider(modifier = Modifier.fillMaxWidth(1f))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseDetails(
    data: ExpenseDetailsViewModel.State.Data,
    onUpdated: (UpdateAction) -> Unit,
) {
    var amount by remember {
        mutableStateOf(if (data.expense.totalAmount.value != 0.0) data.expense.totalAmount.value.toString() else "")
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showCurrencyPicker by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val tutorialControl = LocalTutorialControl.current

    var listenForTitle by remember { mutableStateOf(true) }
    var listenForAmount by remember { mutableStateOf(true) }

    LaunchedEffect(data, amount) {
        if (data.expense.title.length > 4 && listenForTitle) {
            listenForTitle = false
            tutorialControl.onNext()
        }

        if (amount.length > 1 && listenForAmount) {
            listenForAmount = false
            tutorialControl.onNext()
        }
    }

    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            ),
        modifier = Modifier.widthIn(max = 450.dp).fillMaxWidth(1f).padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        TutorialItem(
            onPositioned = { tutorialControl.onPositionRecieved(getStep(OnboardingStep.EXPENSE_TITLE), it) },
        ) { modifier ->
            OutlinedTextField(
                modifier = modifier.fillMaxWidth(1f).padding(horizontal = 16.dp).focusRequester(focusRequester),
                singleLine = true,
                enabled = data.expense.allowedToChange(),
                value = data.expense.title,
                isError = data.expense.title.isNullOrBlank(),
                keyboardOptions =
                    KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Sentences,
                    ),
                supportingText = {
                    if (data.expense.title.isNullOrBlank()) Text("Title should be filled")
                },
                onValueChange = { value -> onUpdated(UpdateAction.Title(value)) },
                prefix = {
                    Row {
                        Icon(
                            imageVector = AdaptiveIcons.Outlined.Create,
                            contentDescription = "Expense title",
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                },
                placeholder = {
                    Text(
                        text = "Expense title",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.height(IntrinsicSize.Max).fillMaxWidth(1f).padding(horizontal = 16.dp),
        ) {
            TutorialItem(
                onPositioned = { tutorialControl.onPositionRecieved(getStep(OnboardingStep.EXPENSE_DATE_CURRENCY), it) },
            ) { modifier ->
                Row(
                    modifier = modifier.height(IntrinsicSize.Max),
                ) {
                    FilledTonalButton(
                        modifier = Modifier.minimumInteractiveComponentSize().fillMaxHeight(1f),
                        enabled = data.expense.allowedToChange(),
                        onClick = { showDatePicker = !showDatePicker },
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        val expenseDate = data.expense.date.toInstant().toLocalDateTime(TimeZone.currentSystemDefault())
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = expenseDate.month.name.substring(0, 3),
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(
                                text = expenseDate.dayOfMonth.toString().padStart(2, '0'),
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledTonalButton(
                        modifier = Modifier.fillMaxHeight(1f),
                        onClick = { showCurrencyPicker = true },
                        enabled = data.expense.allowedToChange(),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text(data.expense.totalAmount.currencyCode.currencySymbol())
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            TutorialItem(
                onPositioned = { tutorialControl.onPositionRecieved(getStep(OnboardingStep.EXPENSE_AMOUNT), it) },
            ) { modifier ->
                TextField(
                    modifier = modifier.fillMaxWidth(1f),
                    singleLine = true,
                    value = amount,
                    enabled = data.expense.allowedToChange(),
                    keyboardOptions =
                        KeyboardOptions(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Decimal,
                        ),
                    isError = amount.isNullOrBlank() || amount.toDoubleOrNull() == 0.0,
                    onValueChange = { value ->
                        if (value.isNullOrBlank()) {
                            amount = ""
                            onUpdated(UpdateAction.TotalAmount(0.0, data.expense.totalAmount.currencyCode))
                        } else {
                            val doubleValue = value.toDoubleOrNull()
                            val filtered = if (doubleValue != null) value else amount
                            amount = filtered
                            doubleValue?.let {
                                onUpdated(UpdateAction.TotalAmount(it, data.expense.totalAmount.currencyCode))
                            }
                        }
                    },
                    placeholder = {
                        Text(
                            text = "Amount",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = "Payed by",
            style = MaterialTheme.typography.labelSmall,
        )

        var payerSelection by remember { mutableStateOf(false) }

        TutorialItem(
            onPositioned = { tutorialControl.onPositionRecieved(getStep(OnboardingStep.EXPENSE_PAYER), it) },
        ) { modifier ->
            ParticipantListItem(
                modifier = modifier,
                participant = data.expense.payedBy,
                enabled = data.expense.allowedToChange(),
                onClick = { payerSelection = true },
                action = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_down),
                        contentDescription = stringResource(Res.string.select_payer_cd),
                    )
                },
            )
        }

        PayerChooser(
            expanded = payerSelection,
            payer = data.expense.payedBy,
            allParticipants = data.allParticipants(),
            onDismiss = { payerSelection = false },
            onUpdated = onUpdated,
        )

        HorizontalDivider()
        ProtectionBlock(onUpdated, data)

        val datePickerState =
            rememberDatePickerState(
                initialSelectedDateMillis = data.expense.date.toInstant().toEpochMilliseconds(),
            )
        val confirmEnabled = remember { derivedStateOf { datePickerState.selectedDateMillis != null } }

        if (showDatePicker) {
            DatePickerDialog(onDismissRequest = {
                datePickerState.selectedDateMillis = data.expense.date.toInstant().toEpochMilliseconds()
                showDatePicker = false
            }, confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    datePickerState.selectedDateMillis?.let {
                        onUpdated(UpdateAction.Date(it))
                    }
                }, enabled = confirmEnabled.value) { Text("OK") }
            }, dismissButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis = data.expense.date.toInstant().toEpochMilliseconds()
                    showDatePicker = false
                }) { Text("Cancel") }
            }) {
                DatePicker(state = datePickerState)
            }
        }

        if (showCurrencyPicker) {
            CurrencyPicker(
                currencies = data.availableCurrencies,
                onDismiss = { showCurrencyPicker = false },
                onConfirm = { currency ->
                    onUpdated(UpdateAction.TotalAmount(data.expense.totalAmount.value, currency))
                },
            )
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun TopAppBareByState(
    state: ExpenseDetailsViewModel.State,
    onAction: (AddExpenseAction) -> Unit,
    onToolbarAction: (AddExpenseTollbarAction) -> Unit,
) {
    AdaptiveTopAppBar(
        title = {
            Text(
                when (state) {
                    ExpenseDetailsViewModel.State.Loading -> stringResource(Res.string.loading)
                    is ExpenseDetailsViewModel.State.Error -> stringResource(Res.string.settings)
                    is ExpenseDetailsViewModel.State.Data ->
                        if (state.expense.id == null) {
                            stringResource(Res.string.new_expense)
                        } else {
                            state.expense.title
                        }
                },
            )
        },
        onNavigationIconClick = { onAction(AddExpenseAction.Back) },
        actions = {
            Box(
                modifier =
                    Modifier.fillMaxHeight(1f).clickable {
                        when (state) {
                            is ExpenseDetailsViewModel.State.Error -> {}

                            is ExpenseDetailsViewModel.State.Data -> onToolbarAction(AddExpenseTollbarAction.Commit)

                            ExpenseDetailsViewModel.State.Loading -> {}
                        }
                    }.padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                when (state) {
                    is ExpenseDetailsViewModel.State.Error ->
                        Text(
                            // TODO: Add leading icon retry icon
                            text = stringResource(Res.string.retry),
                        )

                    is ExpenseDetailsViewModel.State.Data ->
                        if (state.expense.id == null) {
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

                    ExpenseDetailsViewModel.State.Loading -> CircularProgressIndicator()
                }
            }
        },
    )
}

@FutureFeature
@Composable
private fun PayerChooser(
    expanded: Boolean,
    payer: Participant,
    allParticipants: Set<Participant>,
    onDismiss: () -> Unit,
    onUpdated: (UpdateAction) -> Unit,
) {
    DropdownMenu(
        modifier = Modifier.requiredSizeIn(maxHeight = 250.dp, minWidth = 360.dp),
        expanded = expanded,
        onDismissRequest = { onDismiss() },
    ) {
        allParticipants.forEach { participant ->
            DropdownMenuItem(
                text = { ParticipantListItem(participant = participant) },
                onClick = {
                    onUpdated(UpdateAction.NewPayer(participant))
                    onDismiss()
                },
            )
        }
    }
}

@FutureFeature
@Composable
private fun CurrencyChooser(
    expanded: Boolean,
    onUpdated: (UpdateAction) -> Unit,
) {
    var expanded1 = expanded
    DropdownMenu(
        modifier = Modifier.requiredSizeIn(maxHeight = 250.dp),
        expanded = expanded1,
        onDismissRequest = { expanded1 = false },
    ) {
        (0..20).forEach {
            DropdownMenuItem(
                // TODO: Currencies in here
                text = { Text("Item1") },
                onClick = {
                    // TODO: Change selected currency
                    expanded1 = false
                },
            )
        }
    }
}
