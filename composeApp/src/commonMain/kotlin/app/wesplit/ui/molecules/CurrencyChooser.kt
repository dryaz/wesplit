package app.wesplit.ui.molecules

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.currency.CurrencyCodesCollection
import app.wesplit.domain.model.currency.currencySymbol
import app.wesplit.ui.atoms.CurrencyPickerDialog

@Composable
fun CurrencyChooser(
    modifier: Modifier = Modifier,
    selectedCurrencyCode: String,
    availableCurrencies: CurrencyCodesCollection,
    enabled: Boolean = true,
    onSelect: (String) -> Unit,
) {
    var showCurrencyPicker by remember { mutableStateOf(false) }
    FilledTonalButton(
        modifier = modifier,
        onClick = { showCurrencyPicker = true },
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
    ) {
        Text(selectedCurrencyCode.currencySymbol())
    }

    if (showCurrencyPicker) {
        CurrencyPickerDialog(
            currencies = availableCurrencies,
            onDismiss = { showCurrencyPicker = false },
            onConfirm = { currency ->
                onSelect(currency)
            },
        )
    }
}
