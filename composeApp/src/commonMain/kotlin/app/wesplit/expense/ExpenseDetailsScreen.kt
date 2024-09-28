package app.wesplit.expense

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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.FutureFeature
import app.wesplit.domain.model.expense.SplitType
import app.wesplit.domain.model.expense.format
import app.wesplit.domain.model.expense.toInstant
import app.wesplit.domain.model.group.Participant
import app.wesplit.domain.model.group.uiTitle
import app.wesplit.expense.ExpenseDetailsViewModel.State.Loading.allParticipants
import app.wesplit.participant.ParticipantListItem
import app.wesplit.ui.AdaptiveTopAppBar
import io.github.alexzhirkevich.cupertino.adaptive.icons.AdaptiveIcons
import io.github.alexzhirkevich.cupertino.adaptive.icons.Create
import io.github.alexzhirkevich.cupertino.adaptive.icons.Delete
import io.github.alexzhirkevich.cupertino.adaptive.icons.Done
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.add_expense_to_group
import split.composeapp.generated.resources.create
import split.composeapp.generated.resources.ic_down
import split.composeapp.generated.resources.loading
import split.composeapp.generated.resources.new_expense
import split.composeapp.generated.resources.retry
import split.composeapp.generated.resources.save
import split.composeapp.generated.resources.select_payer_cd
import split.composeapp.generated.resources.settings

sealed interface AddExpenseAction {
    data object Back : AddExpenseAction
}

private sealed interface AddExpenseTollbarAction {
    data object Commit : AddExpenseTollbarAction
}

// TODO: Clear hardcoded strings
@Composable
fun ExpenseDetailsScreen(
    modifier: Modifier = Modifier,
    viewModel: ExpenseDetailsViewModel,
    onAction: (AddExpenseAction) -> Unit,
) {
    val state = viewModel.state.collectAsState()

    fun commit() {
        viewModel.update(UpdateAction.Commit)
        onAction(AddExpenseAction.Back)
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    (state.value as? ExpenseDetailsViewModel.State.Data)?.let {
                        if (it.isComplete) commit()
                    }
                    // TODO: Show error in case of it not yet ready?
                },
            ) {
                Icon(
                    AdaptiveIcons.Outlined.Done,
                    contentDescription = stringResource(Res.string.add_expense_to_group),
                )
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
                    if (action == UpdateAction.Delete) {
                        onAction(AddExpenseAction.Back)
                    }
                }
            // TODO: Shimmer?
            ExpenseDetailsViewModel.State.Loading -> Text("Loading")
        }
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

@Composable
private fun SharesDetails(
    data: ExpenseDetailsViewModel.State.Data,
    onUpdated: (UpdateAction) -> Unit,
) {
    var splitType by remember { mutableStateOf(SplitType.EQUAL) }

    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            ),
        modifier = Modifier.widthIn(max = 450.dp).fillMaxWidth(1f).padding(horizontal = 16.dp),
    ) {
        SharesDetailsHeader(data)
        SharesDetailsParticipants(data, splitType, onUpdated)
    }
}

@Composable
fun SharesDetailsParticipants(
    data: ExpenseDetailsViewModel.State.Data,
    splitType: SplitType,
    onUpdated: (UpdateAction) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    var selectedTabIndex by remember {
        mutableIntStateOf(
            when (splitType) {
                SplitType.EQUAL -> 0
                SplitType.SHARES -> 1
                SplitType.AMOUNTS -> TODO("Amounts not yet supported")
            },
        )
    }

    Column {
        TabRow(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            selectedTabIndex = selectedTabIndex,
        ) {
            Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }, text = { Text("Equal") })
            Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }, text = { Text("By Shares") })
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
                }
            }
        }
    }

    LaunchedEffect(key1 = selectedTabIndex) {
        pagerState.animateScrollToPage(selectedTabIndex)
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
            SplitType.EQUAL ->
                ParticipantListItem(
                    participant = participant,
                    onClick = { item ->
                        onUpdated(
                            UpdateAction.Split.Equal(
                                participant = item,
                                isIncluded = data.expense.shares.none { it.participant.id == item.id },
                            ),
                        )
                    },
                    action = {
                        Checkbox(
                            checked = data.expense.shares.any { it.participant.id == participant.id },
                            onCheckedChange = { isChecked ->
                                onUpdated(
                                    UpdateAction.Split.Equal(
                                        participant = participant,
                                        isIncluded = isChecked,
                                    ),
                                )
                            },
                        )
                    },
                    subTitle =
                        data.expense.shares.find { it.participant.id == participant.id }?.let {
                            it.amount.format()
                        } ?: "Not participating",
                )

            SplitType.SHARES ->
                ParticipantListItem(
                    participant = participant,
                    action = {
                        TextField(
                            modifier = Modifier.width(74.dp),
                            singleLine = true,
                            value = data.expense.shares.find { it.participant.id == participant.id }?.amount?.format(false) ?: "0",
                            keyboardOptions =
                                KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                ),
                            onValueChange = { value ->
                                val floatValue = value.toFloatOrNull() ?: 0f
                                onUpdated(
                                    UpdateAction.Split.Share(
                                        participant = participant,
                                        value = floatValue,
                                    ),
                                )
                            },
                        )
                    },
                    subTitle =
                        data.expense.shares.find { it.participant.id == participant.id }?.let {
                            it.amount.format()
                        } ?: "Not participating",
                )

            SplitType.AMOUNTS -> TODO("Amount not supported yet")
        }
    }
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
        mutableStateOf(if (data.expense.totalAmount.value != 0f) data.expense.totalAmount.value.toString() else "")
    }

    var showDatePicker by remember { mutableStateOf(false) }

    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            ),
        modifier = Modifier.widthIn(max = 450.dp).fillMaxWidth(1f).padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(1f).padding(horizontal = 16.dp),
            singleLine = true,
            value = data.expense.title,
            isError = data.expense.title.isNullOrBlank(),
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
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.height(IntrinsicSize.Max).fillMaxWidth(1f).padding(horizontal = 16.dp),
        ) {
            FilledTonalButton(
                modifier = Modifier.minimumInteractiveComponentSize().fillMaxHeight(1f),
                enabled = true,
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
                modifier = Modifier.minimumInteractiveComponentSize().fillMaxHeight(1f),
                enabled = false,
                onClick = { },
                shape = RoundedCornerShape(10.dp),
            ) {
                // TODO: Get currency from group
                Text("$")
            }
            Spacer(modifier = Modifier.width(16.dp))

            TextField(
                modifier = Modifier.fillMaxWidth(1f),
                singleLine = true,
                value = amount,
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                    ),
                isError = amount.isNullOrBlank() || amount.toFloatOrNull() == 0f,
                onValueChange = { value ->
                    if (value.isNullOrBlank()) {
                        amount = ""
                        onUpdated(UpdateAction.TotalAmount(0f))
                    } else {
                        val floatValue = value.toFloatOrNull()
                        val filtered = if (floatValue != null) value else amount
                        amount = filtered
                        floatValue?.let {
                            onUpdated(UpdateAction.TotalAmount(it))
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

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = "Payed by",
            style = MaterialTheme.typography.labelSmall,
        )

        var payerSelection by remember { mutableStateOf(false) }

        // TODO: Change payer action
        ParticipantListItem(
            participant = data.expense.payedBy,
            onClick = { payerSelection = true },
            action = {
                Icon(
                    painter = painterResource(Res.drawable.ic_down),
                    contentDescription = stringResource(Res.string.select_payer_cd),
                )
            },
        )

        PayerChooser(
            expanded = payerSelection,
            payer = data.expense.payedBy,
            allParticipants = data.allParticipants(),
            onDismiss = { payerSelection = false },
            onUpdated = onUpdated,
        )

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

                            is ExpenseDetailsViewModel.State.Data -> if (state.isComplete) onToolbarAction(AddExpenseTollbarAction.Commit)

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
                text = { ParticipantListItem(participant) },
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
