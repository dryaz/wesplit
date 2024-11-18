package app.wesplit.preview

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.currency.Amount
import app.wesplit.domain.model.currency.format
import app.wesplit.domain.model.expense.Expense
import app.wesplit.domain.model.expense.ExpenseType
import app.wesplit.domain.model.expense.myAmount
import app.wesplit.domain.model.group.Participant
import app.wesplit.expense.categoryIconRes
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.ic_date
import split.composeapp.generated.resources.paid_by_you

/**
 * KMP doesn't have proper preview but android do have
 * preview mode + liveedit. So sometimes it makes sense
 * to use playground to build view.
 */
@Composable
@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
fun Playground() =
    ExpenseItem(
        expense =
            Expense(
                title = "123",
                expenseType = ExpenseType.EXPENSE,
                payedBy = Participant(name = "abc"),
                undistributedAmount = null,
                totalAmount = Amount(78.0, "EUR"),
                shares = emptySet(),
            ),
    )

@Composable
private fun ExpenseItem(expense: Expense) {
//    val localeDate = expense.date.toInstant().toLocalDateTime(TimeZone.currentSystemDefault())
    Row(
        modifier =
            Modifier
                .clickable {
                }
                .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Date
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier.size(14.dp).padding(bottom = 1.dp),
                    painter = painterResource(Res.drawable.ic_date),
                    contentDescription = "Current day of month is 24",
                    tint = MaterialTheme.colorScheme.outline,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "24",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            Icon(
                modifier = Modifier.size(32.dp),
                painter = painterResource(expense.category.categoryIconRes()),
                contentDescription = "Expense category is ${expense.category}",
                tint = MaterialTheme.colorScheme.secondary,
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        // TODO: Support category image
        // Title + balance
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = stringResource(Res.string.paid_by_you),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
            )
            Row {
                Text(
                    modifier = Modifier.weight(1f),
                    text = expense.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text("You lent smth")
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
                text = "You: 20 EUR",
                style = MaterialTheme.typography.bodySmall,
                color =
                    if (expense.myAmount().value != 0.0) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    },
            )
        }
    }
}
