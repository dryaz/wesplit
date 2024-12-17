package app.wesplit.quicksplit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.FutureFeature
import app.wesplit.domain.model.group.Participant
import app.wesplit.participant.ParticipantListItem
import app.wesplit.ui.AdaptiveTopAppBar
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.loading
import split.composeapp.generated.resources.paid_by

sealed interface QuickSplitAction {
    data object Back : QuickSplitAction
}

@Composable
fun QuickSplitScreen(
    modifier: Modifier = Modifier,
    viewModel: QuickSplitViewModel,
    onAction: (QuickSplitAction) -> Unit,
) {
    val state = viewModel.state.collectAsState()

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            AdaptiveTopAppBar(
                title = {
                    Text(
                        when (state.value) {
                            is QuickSplitViewModel.State.Loading -> stringResource(Res.string.loading)
                            is QuickSplitViewModel.State.Data -> TODO()
                        },
                    )
                },
                onNavigationIconClick = { onAction(QuickSplitAction.Back) },
            )
        },
    ) { paddings ->
        when (val quickSplitState = state.value) {
            is QuickSplitViewModel.State.Data ->
                QuickSplitView(
                    modifier = Modifier.fillMaxSize(1f).padding(paddings),
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
        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
private fun SharesDetails(
    data: QuickSplitViewModel.State.Data,
    onUpdated: (UpdateAction) -> Unit,
) {
    Card(
        modifier = Modifier.widthIn(max = 450.dp).fillMaxWidth(1f).padding(horizontal = 16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            ),
    ) {
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseDetails(
    data: QuickSplitViewModel.State.Data,
    onUpdated: (UpdateAction) -> Unit,
) {
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
//            CurrencyChooser(
//                modifier = Modifier.fillMaxHeight(1f),
//                selectedCurrencyCode = data.expense.totalAmount.currencyCode,
//                availableCurrencies = data.availableCurrencies,
//                enabled = data.expense.allowedToChange(),
//            ) { currency ->
//                onUpdated(UpdateAction.TotalAmount(data.expense.totalAmount.value, currency))
//            }

            Spacer(modifier = Modifier.width(8.dp))
//            AmountField(
//                modifier = Modifier.fillMaxWidth(1f),
//                value = data.expense.totalAmount.value,
//                enabled = data.expense.allowedToChange(),
//                isError = amount.isNullOrBlank() || amount.toDoubleOrNull() == 0.0,
//            ) { newAmount ->
//                onUpdated(UpdateAction.TotalAmount(newAmount, data.expense.totalAmount.currencyCode))
//            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = stringResource(Res.string.paid_by),
            style = MaterialTheme.typography.labelSmall,
        )

//        var payerSelection by remember { mutableStateOf(false) }

//        ParticipantListItem(
//            modifier = Modifier,
//            participant = data.expense.payedBy,
//            enabled = data.expense.allowedToChange(),
//            onClick = { payerSelection = true },
//            action = {
//                Icon(
//                    painter = painterResource(Res.drawable.ic_down),
//                    contentDescription = stringResource(Res.string.select_payer_cd),
//                )
//            },
//        )
//
//        PayerChooser(
//            expanded = payerSelection,
//            payer = data.expense.payedBy,
//            allParticipants = data.allParticipants(),
//            onDismiss = { payerSelection = false },
//            onUpdated = onUpdated,
//        )
    }
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
//                    onUpdated(UpdateAction.NewPayer(participant))
                    onDismiss()
                },
            )
        }
    }
}
