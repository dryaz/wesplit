package app.wesplit.expense

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import app.wesplit.ui.AdaptiveTopAppBar
import io.github.alexzhirkevich.cupertino.adaptive.ExperimentalAdaptiveApi
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.create
import split.composeapp.generated.resources.loading
import split.composeapp.generated.resources.new_expense
import split.composeapp.generated.resources.retry
import split.composeapp.generated.resources.save
import split.composeapp.generated.resources.settings

sealed interface AddExpenseAction {
    data object Back : AddExpenseAction
}

private sealed interface AddExpenseTollbarAction {
    data object Commit : AddExpenseTollbarAction
}

@Composable
fun AddExpenseScreen(
    modifier: Modifier = Modifier,
    viewModel: AddExpenseViewModel,
    onAction: (AddExpenseAction) -> Unit,
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
                        AddExpenseTollbarAction.Commit -> TODO()
                    }
                },
            )
        },
    ) { paddings ->
        when (val expenseState = state.value) {
            is AddExpenseViewModel.State.Error -> Text("Error")
            is AddExpenseViewModel.State.Expense ->
                AddExpenseScreenView(
                    modifier = Modifier.fillMaxSize(1f).padding(paddings),
                    expense = expenseState,
                    onDone = {
                        TODO("Commit group")
                    },
                ) { group ->
                    TODO("Update group")
                }
            // TODO: Shimmer?
            AddExpenseViewModel.State.Loading -> Text("Loading")
        }
    }
}

@OptIn(ExperimentalAdaptiveApi::class)
@Composable
private fun AddExpenseScreenView(
    modifier: Modifier = Modifier,
    expense: AddExpenseViewModel.State.Expense,
    onDone: () -> Unit,
    onUpdated: (AddExpenseViewModel.State.Expense) -> Unit,
) {
    // TODO: Select payer same way that we select participants
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
                Text("Here is adding ui")
            }
        }
    }

    AnimatedVisibility(visible = userSelectorVisibility) {
        // TODO: Same UI as ParticipantPicker but only from predefine list of current participants
    }
}

@Composable
private fun TopAppBareByState(
    state: AddExpenseViewModel.State,
    onAction: (AddExpenseAction) -> Unit,
    onToolbarAction: (AddExpenseTollbarAction) -> Unit,
) {
    AdaptiveTopAppBar(
        title = {
            Text(
                when (state) {
                    AddExpenseViewModel.State.Loading -> stringResource(Res.string.loading)
                    is AddExpenseViewModel.State.Error -> stringResource(Res.string.settings)
                    is AddExpenseViewModel.State.Expense ->
                        if (state.id == null) {
                            stringResource(Res.string.new_expense)
                        } else {
                            state.title ?: ""
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
                            is AddExpenseViewModel.State.Error -> {}

                            is AddExpenseViewModel.State.Expense ->
                                onToolbarAction(
                                    AddExpenseTollbarAction.Commit,
                                )

                            AddExpenseViewModel.State.Loading -> {}
                        }
                    }.padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                when (state) {
                    is AddExpenseViewModel.State.Error ->
                        Text(
                            // TODO: Add leading icon retry icon
                            text = stringResource(Res.string.retry),
                        )

                    is AddExpenseViewModel.State.Expense ->
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

                    AddExpenseViewModel.State.Loading -> CircularProgressIndicator()
                }
            }
        },
    )
}
