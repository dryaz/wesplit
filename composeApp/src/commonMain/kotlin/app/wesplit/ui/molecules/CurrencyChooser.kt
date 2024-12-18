package app.wesplit.ui.molecules

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.wesplit.domain.model.currency.CurrencyRepository
import app.wesplit.domain.model.currency.currencySymbol
import app.wesplit.ui.atoms.CurrencyPickerDialog
import org.koin.compose.koinInject

@Composable
fun CurrencyChooser(
    modifier: Modifier = Modifier,
    selectedCurrencyCode: String,
    enabled: Boolean = true,
    onSelect: (String) -> Unit,
) {
    val currencyRepository: CurrencyRepository = koinInject()
    val viewModel =
        viewModel {
            CurrencyChooserViewModel(currencyRepository)
        }

    val availableCurrencies = viewModel.currencies.collectAsState()
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
            currencies = availableCurrencies.value,
            onDismiss = { showCurrencyPicker = false },
            onConfirm = { currency ->
                onSelect(currency)
            },
        )
    }
}
