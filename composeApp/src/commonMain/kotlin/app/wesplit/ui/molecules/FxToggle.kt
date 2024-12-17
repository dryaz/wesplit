package app.wesplit.ui.molecules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.currency.CurrencyCodesCollection
import app.wesplit.domain.model.currency.FxState
import app.wesplit.domain.model.currency.currencySymbol
import app.wesplit.ui.PlusProtected
import app.wesplit.ui.atoms.CurrencyPicker
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.fx_single_currency
import split.composeapp.generated.resources.recalculate

data class FxToggleState(
    val fxRates: FxState,
    val selectedCurrency: String,
    val currencyCodesCollection: CurrencyCodesCollection,
    val isRecalculateEnabled: Boolean,
)

sealed interface FxToggleAction {
    data class CurrencyChanged(val newCurrency: String) : FxToggleAction

    data class Toggle(val isChecked: Boolean) : FxToggleAction
}

@Composable
internal fun FxToggle(
    state: FxToggleState,
    onAction: (FxToggleAction) -> Unit,
) {
    var showCurrencyPicker by remember { mutableStateOf(false) }
    val protectCallback: (Boolean) -> Unit =
        remember {
            { onAction(FxToggleAction.Toggle(it)) }
        }

    val clickabeModifier =
        if (state.fxRates !is FxState.Loading) {
            Modifier.clickable { protectCallback(!state.isRecalculateEnabled) }
        } else {
            Modifier
        }

    ListItem(
        modifier = clickabeModifier.height(IntrinsicSize.Max),
        leadingContent = {
            when (state.fxRates) {
                is FxState.Data,
                is FxState.Error,
                ->
                    FilledTonalButton(
                        modifier = Modifier.fillMaxHeight(1f),
                        onClick = { showCurrencyPicker = true },
                        enabled = state.fxRates !is FxState.Loading,
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text(state.selectedCurrency.currencySymbol())
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
                    text = stringResource(Res.string.recalculate),
                )
            }
        },
        supportingContent = {
            Text(
                text = stringResource(Res.string.fx_single_currency),
            )
        },
        colors =
            ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            ),
        trailingContent = {
            Switch(
                checked = state.isRecalculateEnabled,
                enabled = state.fxRates !is FxState.Loading,
                onCheckedChange = { protectCallback(it) },
            )
        },
    )

    if (showCurrencyPicker) {
        CurrencyPicker(
            currencies = state.currencyCodesCollection,
            onDismiss = { showCurrencyPicker = false },
            onConfirm = { currency ->
                onAction(FxToggleAction.CurrencyChanged(currency))
            },
        )
    }
}
