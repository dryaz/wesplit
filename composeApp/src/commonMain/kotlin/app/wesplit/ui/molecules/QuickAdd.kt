package app.wesplit.ui.molecules

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
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.currency.currencySymbol
import app.wesplit.filterDoubleInput
import io.github.alexzhirkevich.cupertino.adaptive.icons.AdaptiveIcons
import io.github.alexzhirkevich.cupertino.adaptive.icons.Clear
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.amount
import split.composeapp.generated.resources.cancel
import split.composeapp.generated.resources.expense_title

sealed interface QuickAddState {
    data object Hidden : QuickAddState

    data object Paywall : QuickAddState

    data class Data(
        val value: QuickAddValue,
        val error: QuickAddErrorState,
    ) : QuickAddState
}

data class QuickAddValue(
    val title: String = "",
    val currencyCode: String? = null,
    val amount: Double? = null,
)

fun QuickAddValue.isEmpty(): Boolean = title.isNullOrBlank() && (amount ?: 0.0) == 0.0

sealed interface QuickAddAction {
    data object Commit : QuickAddAction

    data object RequestPaywall : QuickAddAction

    data class Change(val value: QuickAddValue?) : QuickAddAction
}

enum class QuickAddErrorState {
    NONE,
    TITLE,
    AMOUNT,
}

@Composable
fun QuickAdd(
    modifier: Modifier = Modifier,
    state: QuickAddState,
    onAction: (QuickAddAction) -> Unit,
) = when (state) {
    is QuickAddState.Data ->
        QuickAddControl(
            modifier = modifier,
            state = state.value,
            error = state.error,
            onAction = onAction,
        )

    QuickAddState.Paywall ->
        QuickAddPaywall(
            modifier = modifier,
            onAction = onAction,
        )

    QuickAddState.Hidden -> Unit
}

@Composable
private fun QuickAddPaywall(
    modifier: Modifier = Modifier,
    onAction: (QuickAddAction) -> Unit,
) {
    val titleFocusRequester = remember { FocusRequester() }
    val amountFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val focusListener: (FocusState) -> Unit =
        remember {
            { state ->
                if (state.isFocused) {
                    focusManager.clearFocus(true)
                    titleFocusRequester.freeFocus()
                    amountFocusRequester.freeFocus()
                    onAction(QuickAddAction.RequestPaywall)
                }
            }
        }

    Row(
        modifier = modifier.fillMaxWidth(1f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextField(
            modifier = Modifier.weight(3f).focusRequester(titleFocusRequester).focusTarget().onFocusChanged(focusListener),
            value = "",
            onValueChange = { },
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
            modifier = Modifier.weight(2f).focusRequester(amountFocusRequester).focusTarget().onFocusChanged(focusListener),
            value = "",
            onValueChange = { },
            label = {
                Text(
                    text = stringResource(Res.string.amount) + " ($)",
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
            maxLines = 1,
        )
    }
}

@Composable
private fun QuickAddControl(
    modifier: Modifier = Modifier,
    state: QuickAddValue,
    error: QuickAddErrorState = QuickAddErrorState.NONE,
    onAction: (QuickAddAction) -> Unit,
) {
    val titleFocusRequester = remember { FocusRequester() }
    val amountFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    var title by remember(state) { mutableStateOf(state.title) }
    var doubleAmount by remember(state) { mutableStateOf(state.amount ?: 0.0) }
    var amount: String by remember(state) {
        mutableStateOf(
            state.amount?.let {
                if (it != 0.0) it.toString() else ""
            } ?: "",
        )
    }

    LaunchedEffect(title, doubleAmount) {
        onAction(
            QuickAddAction.Change(
                if (title.isNullOrBlank() && doubleAmount == 0.0) {
                    null
                } else {
                    QuickAddValue(
                        title = title,
                        currencyCode = state.currencyCode,
                        amount = doubleAmount,
                    )
                },
            ),
        )
    }

    LaunchedEffect(state) {
        if (state.amount == null && state.title.isNullOrBlank()) {
            focusManager.clearFocus(true)
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
            onValueChange = { newValue ->
                val newFilteredValue = newValue.filterDoubleInput()
                val isEndingWithDot = newFilteredValue.endsWith(".")
                var needUpdate = false

                if (!isEndingWithDot) {
                    needUpdate = true
                }

                amount = newFilteredValue

                if (needUpdate) {
                    doubleAmount = newFilteredValue.toDoubleOrNull() ?: 0.0
                }
            },
            label = {
                Text(
                    text = stringResource(Res.string.amount) + " (${(state.currencyCode ?: "USD").currencySymbol()})",
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
            visible = doubleAmount > 0.0 || title.length > 0,
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
