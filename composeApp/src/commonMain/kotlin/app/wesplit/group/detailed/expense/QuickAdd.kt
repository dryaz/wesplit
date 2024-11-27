package app.wesplit.group.detailed.expense

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.currency.currencySymbol
import io.github.alexzhirkevich.cupertino.adaptive.icons.AdaptiveIcons
import io.github.alexzhirkevich.cupertino.adaptive.icons.Clear
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.amount
import split.composeapp.generated.resources.cancel
import split.composeapp.generated.resources.expense_title

data class QuickAddValue(
    val title: String,
    val currencyCode: String,
    val amount: Double?,
)

sealed interface QuickAddAction {
    data object Commit : QuickAddAction

    data class Change(val value: QuickAddValue?) : QuickAddAction
}

enum class QuickAddErrorState {
    NONE,
    TITLE,
    AMOUNT,
}

@Composable
internal fun QuickAdd(
    modifier: Modifier = Modifier,
    value: QuickAddValue,
    error: QuickAddErrorState = QuickAddErrorState.NONE,
    onAction: (QuickAddAction) -> Unit,
) {
    val titleFocusRequester = remember { FocusRequester() }
    val amountFocusRequester = remember { FocusRequester() }

    var title by remember(value) { mutableStateOf(value.title) }
    var amount by remember(value) { mutableStateOf(value.amount?.toString() ?: "") }

    LaunchedEffect(title, amount) {
        onAction(
            QuickAddAction.Change(
                if (title.isNullOrBlank() && amount.isNullOrBlank()) {
                    null
                } else {
                    QuickAddValue(
                        title = title,
                        currencyCode = value.currencyCode,
                        amount = amount.toDoubleOrNull(),
                    )
                },
            ),
        )
    }

    LaunchedEffect(value) {
        if (value.amount == null && value.title.isNullOrBlank()) {
            titleFocusRequester.freeFocus()
            amountFocusRequester.freeFocus()
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(1f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextField(
            modifier = Modifier.weight(3f).focusRequester(titleFocusRequester),
            value = title,
            isError = error == QuickAddErrorState.TITLE,
            onValueChange = { title = it },
            label = {
                Text(
                    text = stringResource(Res.string.expense_title),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            colors =
                TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            singleLine = true,
            keyboardOptions =
                KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Sentences,
                ),
            maxLines = 1,
        )
        Spacer(modifier = Modifier.width(8.dp))
        TextField(
            modifier = Modifier.weight(2f).focusRequester(amountFocusRequester),
            value = amount,
            isError = error == QuickAddErrorState.AMOUNT,
            onValueChange = { value ->
                if (value.isNullOrBlank()) {
                    amount = ""
                } else {
                    val doubleValue = value.toDoubleOrNull()
                    val filtered = if (doubleValue != null) value else amount
                    amount = filtered
                }
            },
            label = {
                Text(
                    text = stringResource(Res.string.amount) + " (${value.currencyCode.currencySymbol()})",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            colors =
                TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done,
                ),
            keyboardActions =
                KeyboardActions {
                    onAction(QuickAddAction.Commit)
                },
            singleLine = true,
            maxLines = 1,
        )
        AnimatedVisibility(
            modifier = Modifier.padding(start = 8.dp),
            visible = amount.length > 0 || title.length > 0,
        ) {
            IconButton(
                modifier = Modifier.minimumInteractiveComponentSize(),
                onClick = {
                    onAction(QuickAddAction.Change(null))
                },
            ) {
                Icon(
                    AdaptiveIcons.Outlined.Clear,
                    contentDescription = stringResource(Res.string.cancel),
                )
            }
        }
    }
}
