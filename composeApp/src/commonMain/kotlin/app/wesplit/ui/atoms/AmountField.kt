package app.wesplit.ui.atoms

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import app.wesplit.filterDoubleInput
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.amount

@Composable
fun AmountField(
    modifier: Modifier = Modifier,
    value: Double,
    enabled: Boolean = true,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions =
        KeyboardOptions(
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Decimal,
        ),
    onValueChanged: (Double) -> Unit,
) {
    var amount by remember(value) {
        mutableStateOf(if (value != 0.0) value.toString() else "")
    }

    TextField(
        modifier = modifier,
        singleLine = true,
        value = amount,
        enabled = enabled,
        keyboardOptions = keyboardOptions,
        isError = isError,
        onValueChange = { newValue ->
            val newFilteredValue = newValue.filterDoubleInput()
            val isEndingWithDot = newFilteredValue.endsWith(".")
            var needUpdate = false

            if (!isEndingWithDot) {
                needUpdate = true
            }

            amount = newFilteredValue

            if (needUpdate) {
                onValueChanged(newFilteredValue.toDoubleOrNull() ?: 0.0)
            }
        },
        placeholder = {
            Text(
                text = stringResource(Res.string.amount),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
    )
}
