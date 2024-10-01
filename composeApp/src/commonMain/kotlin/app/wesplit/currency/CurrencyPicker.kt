package app.wesplit.currency

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import app.wesplit.domain.model.currency.currencySymbol
import app.wesplit.expense.ExpenseDetailsViewModel

@Composable
fun CurrencyPicker(
    data: ExpenseDetailsViewModel.State.Data,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Card(
            modifier = Modifier.padding(16.dp).widthIn(max = 240.dp).heightIn(max = 360.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            ListItem(
                headlineContent = { Text("Select currnecy") },
                supportingContent = { Text("For this very expense") },
                colors =
                    ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
            )
            HorizontalDivider()
            // TODO: Search for currency
            LazyColumn(
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh),
            ) {
                items(data.availableCurrencies.lru, key = { "LRU-$it" }) { currency ->
                    ListItem(
                        modifier =
                            Modifier.clickable {
                                onConfirm(currency)
                                onDismiss()
                            },
                        headlineContent = {
                            Text("$currency (${currency.currencySymbol()})")
                        },
                        colors =
                            ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            ),
                    )
                }
                item {
                    HorizontalDivider()
                }
                items(data.availableCurrencies.all, key = { it }) { currency ->
                    ListItem(
                        modifier =
                            Modifier.clickable {
                                onConfirm(currency)
                                onDismiss()
                            },
                        headlineContent = {
                            Text("$currency (${currency.currencySymbol()})")
                        },
                        colors =
                            ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            ),
                    )
                }
            }
        }
    }
}
