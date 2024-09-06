package app.wesplit.preview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.wesplit.data.firebase.fakeData
import app.wesplit.domain.model.expense.Amount
import app.wesplit.domain.model.expense.Expense
import app.wesplit.domain.model.expense.format
import app.wesplit.domain.model.expense.myAmount
import app.wesplit.theme.AppTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * KMP doesn't have proper preview but android do have
 * preview mode + liveedit. So sometimes it makes sense
 * to use playground to build view.
 *
 * NB: AppTheme can't be used here 'cause it inects custom fonts
 * Maybe it make sense to create custom theme with default fonts
 * in order to support at least colors.
 */
@Composable
@Preview(showSystemUi = true)
fun Playground() =
    AppTheme {
        ExpenseList(expenses = fakeData())
    }

@Composable
fun ExpenseList(expenses: List<Expense>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(1f),
    ) {
        items(items = expenses, key = { it.id }) { expense ->
            ExpenseItem(expense)
        }
    }
}

@Composable
fun ExpenseItem(expense: Expense) {
    val localeDate = expense.date.toLocalDateTime(TimeZone.currentSystemDefault())
    Row(
        modifier = Modifier.padding(16.dp),
    ) {
        // Date
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = localeDate.month.name.substring(0, 3),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = localeDate.dayOfMonth.toString().padStart(2, '0'),
                style = MaterialTheme.typography.titleLarge,
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        // TODO: Support category image
        // Title + balance
        Column {
            Text(
                text = expense.title,
                style = MaterialTheme.typography.bodyMedium,
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
            )
            Text(
                text = "You: ${expense.myAmount().amount}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun LentString(expense: Expense) {
    if (expense.myAmount().amount == 0f) {
        Text(
            text = "You're not participating",
            style = MaterialTheme.typography.bodyMedium,
            // TODO: neutral color
        )
    } else if (expense.payedBy.isMe) {
        val lent =
            Amount(
                amount = expense.totalAmount.amount - expense.myAmount().amount,
                currencyCode = expense.totalAmount.currencyCode,
            )
        Text(
            text = "You lent: ${lent.format()}",
            style = MaterialTheme.typography.bodyMedium,
            // TODO: positive color
        )
    } else {
        Text(
            text = "You borrowed: ${expense.myAmount().format()}",
            style = MaterialTheme.typography.bodyMedium,
            // TODO: negative color
        )
    }
}
