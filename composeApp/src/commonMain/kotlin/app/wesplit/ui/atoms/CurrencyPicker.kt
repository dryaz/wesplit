package app.wesplit.ui.atoms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import app.wesplit.domain.model.currency.CurrencyCodesCollection
import app.wesplit.domain.model.currency.currencySymbol
import io.github.alexzhirkevich.cupertino.adaptive.icons.AdaptiveIcons
import io.github.alexzhirkevich.cupertino.adaptive.icons.Search
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.not_found
import split.composeapp.generated.resources.search_currency
import split.composeapp.generated.resources.select_currency

@Composable
fun CurrencyPicker(
    currencies: CurrencyCodesCollection,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
    ) {
        var query by remember { mutableStateOf("") }
        val filteredValues =
            remember {
                derivedStateOf {
                    if (query.isNullOrBlank()) {
                        currencies
                    } else {
                        currencies.copy(
                            lru = emptyList(),
                            all = currencies.all.filter { it.contains(other = query, ignoreCase = true) },
                        )
                    }
                }
            }

        Card(
            modifier = Modifier.padding(16.dp).widthIn(max = 300.dp).heightIn(max = 400.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            TextField(
                modifier = Modifier.fillMaxWidth(1f),
                colors =
                    TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
                placeholder = { Text(stringResource(Res.string.select_currency)) },
                value = query,
                onValueChange = { newValue ->
                    if (newValue.length < 4) query = newValue
                },
                suffix = {
                    Icon(
                        imageVector = AdaptiveIcons.Outlined.Search,
                        contentDescription = stringResource(Res.string.search_currency),
                    )
                },
            )
            HorizontalDivider()
            AnimatedVisibility(
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh),
                visible = filteredValues.value.all.size == 0 && filteredValues.value.lru.size == 0,
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(1f).minimumInteractiveComponentSize(),
                    text = stringResource(Res.string.not_found, query),
                    textAlign = TextAlign.Center,
                )
            }
            LazyColumn(
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh),
            ) {
                items(filteredValues.value.lru, key = { "LRU-$it" }) { currency ->
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
                if (filteredValues.value.lru.isNotEmpty()) {
                    item {
                        HorizontalDivider()
                    }
                }
                items(filteredValues.value.all, key = { it }) { currency ->
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
