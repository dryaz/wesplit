package app.wesplit.group.detailed.expense

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.expense.Amount
import app.wesplit.domain.model.expense.Expense
import app.wesplit.domain.model.expense.format
import app.wesplit.domain.model.expense.myAmount
import app.wesplit.domain.model.expense.toInstant
import app.wesplit.domain.model.group.isMe
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.ic_flag
import split.composeapp.generated.resources.non_distr_cd

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpenseList(
    expenses: Map<String, List<Expense>>,
    onAction: (ExpenseAction) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(1f),
    ) {
        expenses.forEach { entry ->
            stickyHeader {
                Text(
                    modifier =
                        Modifier
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth(1f),
                    text = "${entry.key}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            items(items = entry.value, key = { it.id ?: it.hashCode() }) { expense ->
                ExpenseItem(
                    expense = expense,
                    onAction = onAction,
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun ExpenseItem(
    expense: Expense,
    onAction: (ExpenseAction) -> Unit,
) {
    val localeDate = expense.date.toInstant().toLocalDateTime(TimeZone.currentSystemDefault())
    Row(
        modifier =
            Modifier
                .clickable {
                    onAction(ExpenseAction.OpenDetails(expense))
                }
                .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Date
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = localeDate.month.name.substring(0, 3),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )
            Text(
                text = localeDate.dayOfMonth.toString().padStart(2, '0'),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.outline,
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        // TODO: Support category image
        // Title + balance
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = if (expense.payedBy.isMe()) "Payed by You" else "Payed by ${expense.payedBy.name}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
            )
            Text(
                text = expense.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            LentString(expense)
        }
        Spacer(modifier = Modifier.width(16.dp))
        // Total sum + your cat
        Column(
            modifier = Modifier.widthIn(min = 96.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                text = "${expense.totalAmount.format()}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary,
            )
            Text(
                text = "You: ${expense.myAmount().value}",
                style = MaterialTheme.typography.bodySmall,
                color =
                    if (expense.myAmount().value != 0f) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    },
            )
        }
    }
}

@Composable
private fun LentString(expense: Expense) {
    if (expense.undistributedAmount != null && expense.undistributedAmount?.value != 0f) {
        val undistributed = expense.undistributedAmount
        Row(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(16.dp),
                painter = painterResource(Res.drawable.ic_flag),
                contentDescription = stringResource(Res.string.non_distr_cd),
                tint = MaterialTheme.colorScheme.onErrorContainer,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Not split: ${undistributed?.format()}!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    } else if (expense.myAmount().value == 0f) {
        Text(
            text = "You're not participating",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    } else if (expense.payedBy.isMe()) {
        val lent =
            Amount(
                value = expense.totalAmount.value - expense.myAmount().value,
                currencyCode = expense.totalAmount.currencyCode,
            )
        if (lent.value != 0f) {
            Text(
                text = "You lent: ${lent.format()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        } else {
            Text(
                text = "Settled",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }
    } else {
        Text(
            text = "You borrowed: ${expense.myAmount().format()}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
    }
}
