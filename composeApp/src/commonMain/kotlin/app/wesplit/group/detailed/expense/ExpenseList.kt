package app.wesplit.group.detailed.expense

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.expense.Amount
import app.wesplit.domain.model.expense.Expense
import app.wesplit.domain.model.expense.format
import app.wesplit.domain.model.expense.myAmount
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpenseList(expenses: Map<String, List<Expense>>) {
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
            items(items = entry.value, key = { it.id }) { expense ->
                ExpenseItem(expense)
            }
        }
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun ExpenseItem(expense: Expense) {
    val localeDate = expense.date.toLocalDateTime(TimeZone.currentSystemDefault())
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
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
        Column {
            Text(
                text = "Payed by ${expense.payedBy.name}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
            )
            Text(
                text = expense.title,
                style = MaterialTheme.typography.bodyLarge,
            )
            LentString(expense)
        }
        Spacer(modifier = Modifier.weight(1f))
        // Total sum + your cat
        Column(
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                text = "${expense.totalAmount.format()}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary,
            )
            Text(
                text = "You: ${expense.myAmount().value}",
                style = MaterialTheme.typography.bodyMedium,
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
    if (expense.myAmount().value == 0f) {
        Text(
            text = "You're not participating",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    } else if (expense.payedBy.isMe) {
        val lent =
            Amount(
                value = expense.totalAmount.value - expense.myAmount().value,
                currencyCode = expense.totalAmount.currencyCode,
            )
        Text(
            text = "You lent: ${lent.format()}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    } else {
        Text(
            text = "You borrowed: ${expense.myAmount().format()}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
    }
}
