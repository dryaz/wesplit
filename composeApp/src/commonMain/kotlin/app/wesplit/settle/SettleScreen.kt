package app.wesplit.settle

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.wesplit.ShareDelegate
import app.wesplit.currency.CurrencyPicker
import app.wesplit.domain.model.currency.CurrencyCodesCollection
import app.wesplit.domain.model.currency.FxState
import app.wesplit.domain.model.currency.currencySymbol
import app.wesplit.domain.model.group.Group
import app.wesplit.theme.extraColorScheme
import app.wesplit.ui.AdaptiveTopAppBar
import app.wesplit.ui.PlusProtected
import io.github.alexzhirkevich.cupertino.adaptive.icons.AdaptiveIcons
import io.github.alexzhirkevich.cupertino.adaptive.icons.Done
import io.github.alexzhirkevich.cupertino.adaptive.icons.Info
import io.github.alexzhirkevich.cupertino.adaptive.icons.Share
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.share_group

sealed interface SettleAction {
    data object Back : SettleAction

    data class Share(val group: Group) : SettleAction
}

@Composable
fun SettleScreen(
    modifier: Modifier = Modifier,
    viewModel: SettleViewModel,
    shareDelegate: ShareDelegate,
    onAction: (SettleAction) -> Unit,
) {
    val uiState = viewModel.uiState.collectAsState()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    fun showLinkCopiedSnackbar() {
        scope.launch {
            snackbarHostState.showSnackbar("Sharable link copied!")
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            AdaptiveTopAppBar(title = {
                Text(
                    text =
                        when (val state = uiState.value) {
                            is SettleViewModel.UiState.Data -> "Settle balances"
                            is SettleViewModel.UiState.Error -> "Can't fetch group info"
                            SettleViewModel.UiState.Loading -> "Loading..."
                        },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }, onNavigationIconClick = { onAction(SettleAction.Back) }, actions = {
                IconButton(
                    modifier = modifier,
                    onClick = {
                        (uiState.value as? SettleViewModel.UiState.Data)?.let {
                            if (!shareDelegate.supportPlatformSharing()) showLinkCopiedSnackbar()
                            onAction(SettleAction.Share(it.group))
                        }
                    },
                ) {
                    Icon(
                        AdaptiveIcons.Outlined.Share,
                        contentDescription = stringResource(Res.string.share_group),
                    )
                }
            })
        },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize(1f).widthIn(max = 500.dp).padding(top = padding.calculateTopPadding()),
            contentAlignment = Alignment.Center,
        ) {
            when (val state = uiState.value) {
                is SettleViewModel.UiState.Data ->
                    SettleContent(
                        viewModel = viewModel,
                        state = state,
                    ) {
                        viewModel.settleAll()
                        onAction(SettleAction.Back)
                    }

                is SettleViewModel.UiState.Error -> Text("Error") // TODO: Error state
                SettleViewModel.UiState.Loading ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator()
                    }
            }
        }
    }
}

@Composable
private fun SettleContent(
    viewModel: SettleViewModel,
    state: SettleViewModel.UiState.Data,
    onSettle: () -> Unit,
) {
    var settleDialogShown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(1f).verticalScroll(rememberScrollState()),
    ) {
        SettleBalanceList(
            balance = state.participantBalances,
            footer = {
                FxBlock(
                    fxRates = state.fxRates,
                    selectedCurrency = state.selectedCurrency,
                    currencyCodesCollection = state.currencyCodesCollection,
                    isRecalculateEnabled = state.recalculationEnabled,
                    onCurrencyChanged = { viewModel.selectCurrency(it) },
                    onToggle = { viewModel.toggleRecalculation(it) },
                )
            },
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            Icon(
                imageVector = AdaptiveIcons.Outlined.Info,
                contentDescription = "Info sign",
                tint = MaterialTheme.extraColorScheme.infoContainer,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                modifier = Modifier.weight(1f),
                text =
                    "+X means that person need to be payed back. -Y means that person owse that amount. " +
                        "Do repayment and then press the button below.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        FilledTonalButton(
            modifier =
                Modifier
                    .height(52.dp)
                    .padding(4.dp)
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(1f),
            colors =
                ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            onClick = { settleDialogShown = true },
            shape = RoundedCornerShape(10.dp),
        ) {
            Text(
                text = "We've settled!",
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.7.sp,
                    ),
            )
        }
    }

    if (settleDialogShown) {
        AlertDialog(
            modifier = Modifier.widthIn(max = 450.dp),
            onDismissRequest = { settleDialogShown = false },
            title = { Text("Settle all balances?") },
            text = {
                Text(
                    text = "Are you sure that you want to\n mark all current expenses as settled?",
                    textAlign = TextAlign.Center,
                )
            },
            icon = {
                Icon(
                    AdaptiveIcons.Outlined.Done,
                    contentDescription = "Settled all expenses",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSettle()
                        settleDialogShown = false
                    },
                ) {
                    Text(
                        text = "Yes, Settle",
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        settleDialogShown = false
                    },
                ) {
                    Text(
                        text = "No, Wait",
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            },
        )
    }
}

@Composable
private fun FxBlock(
    fxRates: FxState,
    selectedCurrency: String,
    currencyCodesCollection: CurrencyCodesCollection,
    isRecalculateEnabled: Boolean,
    onCurrencyChanged: (String) -> Unit,
    onToggle: (Boolean) -> Unit,
) {
    var showCurrencyPicker by remember { mutableStateOf(false) }
    val protectCallback: (Boolean) -> Unit =
        remember {
            { onToggle(it) }
        }

    val clickabeModifier =
        if (fxRates !is FxState.Loading) {
            Modifier.clickable { protectCallback(!isRecalculateEnabled) }
        } else {
            Modifier
        }

    ListItem(
        modifier = clickabeModifier.height(IntrinsicSize.Max),
        leadingContent = {
            when (fxRates) {
                is FxState.Data,
                is FxState.Error,
                ->
                    FilledTonalButton(
                        modifier = Modifier.fillMaxHeight(1f),
                        onClick = { showCurrencyPicker = true },
                        enabled = fxRates !is FxState.Loading,
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text(selectedCurrency.currencySymbol())
                    }

                FxState.Loading ->
                    Box(modifier = Modifier.size(52.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
            }
        },
        headlineContent = {
            PlusProtected {
                Text(
                    text = "Recalculate",
                )
            }
        },
        supportingContent = {
            Text(
                text = "FX to single currency",
            )
        },
        colors =
            ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            ),
        trailingContent = {
            Switch(
                checked = isRecalculateEnabled,
                enabled = fxRates !is FxState.Loading,
                onCheckedChange = { protectCallback(it) },
            )
        },
    )

    if (showCurrencyPicker) {
        CurrencyPicker(
            currencies = currencyCodesCollection,
            onDismiss = { showCurrencyPicker = false },
            onConfirm = { currency ->
                onCurrencyChanged(currency)
            },
        )
    }
}
