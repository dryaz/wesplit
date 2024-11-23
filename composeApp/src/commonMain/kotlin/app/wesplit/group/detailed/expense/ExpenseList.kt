package app.wesplit.group.detailed.expense

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.currency.Amount
import app.wesplit.domain.model.currency.format
import app.wesplit.domain.model.expense.Expense
import app.wesplit.domain.model.expense.ExpenseStatus
import app.wesplit.domain.model.expense.myAmount
import app.wesplit.domain.model.expense.toInstant
import app.wesplit.domain.model.group.Group
import app.wesplit.domain.model.group.isMe
import app.wesplit.expense.category.categoryIconRes
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.empty_transaction_description
import split.composeapp.generated.resources.empty_transactions_cd
import split.composeapp.generated.resources.ic_flag
import split.composeapp.generated.resources.img_search_empty
import split.composeapp.generated.resources.non_distr_cd
import split.composeapp.generated.resources.not_participating
import split.composeapp.generated.resources.not_settled
import split.composeapp.generated.resources.not_split
import split.composeapp.generated.resources.not_split_amount
import split.composeapp.generated.resources.paid_by_me
import split.composeapp.generated.resources.paid_by_person
import split.composeapp.generated.resources.paid_by_you
import split.composeapp.generated.resources.personal_expense
import split.composeapp.generated.resources.settled
import split.composeapp.generated.resources.with_me
import split.composeapp.generated.resources.you_borrowed
import split.composeapp.generated.resources.you_lent
import split.composeapp.generated.resources.your_share

enum class FilterType {
    NOT_SETTLED,
    NOT_SPLIT,
    WITH_ME,
    PAYED_ME,
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpenseList(
    group: Group,
    expenses: Map<String, List<Expense>>,
    onAction: (ExpenseAction) -> Unit,
) {
    val filters = remember { mutableStateListOf<FilterType>(FilterType.NOT_SETTLED) }
    // TODO: Maybe move to VM when do pagination etc.
    val dataUnderFilters =
        remember(expenses, filters.size) {
            val notSplit = filters.contains(FilterType.NOT_SPLIT)
            val withMe = filters.contains(FilterType.WITH_ME)
            val payedByMe = filters.contains(FilterType.PAYED_ME)
            val notSettled = filters.contains(FilterType.NOT_SETTLED)
            expenses.mapNotNull {
                val expensesUnderFilter =
                    it.value.filter {
                        var result = true
                        if (notSplit) {
                            result = (it.undistributedAmount?.value ?: 0.0) != 0.0
                        }
                        if (withMe) {
                            result = result && it.myAmount().value != 0.0
                        }
                        if (payedByMe) {
                            result = result && it.payedBy.isMe()
                        }
                        if (notSettled) {
                            result = result && it.status != ExpenseStatus.SETTLED
                        }
                        result
                    }
                if (expensesUnderFilter.isNullOrEmpty()) {
                    null
                } else {
                    it.key to expensesUnderFilter
                }
            }.toMap()
        }

    AnimatedVisibility(
        visible = dataUnderFilters.isEmpty(),
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        EmptyExpenseSection(modifier = Modifier.padding(top = 70.dp, bottom = 16.dp))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(1f),
    ) {
        item {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp).padding(top = 8.dp).horizontalScroll(rememberScrollState()),
            ) {
                FilterChip(
                    selected = filters.contains(FilterType.NOT_SETTLED),
                    onClick = {
                        if (filters.contains(
                                FilterType.NOT_SETTLED,
                            )
                        ) {
                            filters.remove(FilterType.NOT_SETTLED)
                        } else {
                            filters.add(FilterType.NOT_SETTLED)
                        }
                    },
                    label = { Text(stringResource(Res.string.not_settled)) },
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = filters.contains(FilterType.NOT_SPLIT),
                    onClick = {
                        if (filters.contains(
                                FilterType.NOT_SPLIT,
                            )
                        ) {
                            filters.remove(FilterType.NOT_SPLIT)
                        } else {
                            filters.add(FilterType.NOT_SPLIT)
                        }
                    },
                    label = { Text(stringResource(Res.string.not_split)) },
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = filters.contains(FilterType.WITH_ME),
                    onClick = {
                        if (filters.contains(
                                FilterType.WITH_ME,
                            )
                        ) {
                            filters.remove(FilterType.WITH_ME)
                        } else {
                            filters.add(FilterType.WITH_ME)
                        }
                    },
                    label = { Text(stringResource(Res.string.with_me)) },
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = filters.contains(FilterType.PAYED_ME),
                    onClick = {
                        if (filters.contains(
                                FilterType.PAYED_ME,
                            )
                        ) {
                            filters.remove(FilterType.PAYED_ME)
                        } else {
                            filters.add(FilterType.PAYED_ME)
                        }
                    },
                    label = { Text(stringResource(Res.string.paid_by_me)) },
                )
            }
        }

        item {
            PieSampleView(dataUnderFilters.flatMap { it.value })
        }

        dataUnderFilters.forEach { entry ->
            stickyHeader {
                Text(
                    modifier =
                        Modifier
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth(1f),
                    text = "${entry.key}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            items(items = entry.value, key = { it.id ?: it.hashCode() }) { expense ->
                ExpenseItem(
                    group = group,
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
    group: Group,
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
            val dayOfMonth = localeDate.dayOfMonth.toString().padStart(2, '0')
            val month = localeDate.monthNumber.toString().padStart(2, '0') // localeDate.month.name.substring(0, 3)
            Text(
                text = "$dayOfMonth.$month",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )
            Spacer(modifier = Modifier.height(4.dp))
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
                text =
                    if (expense.payedBy.isMe(group)) {
                        stringResource(Res.string.paid_by_you)
                    } else {
                        stringResource(Res.string.paid_by_person, expense.payedBy.name)
                    },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
            )
            Text(
                text = expense.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            LentString(group, expense)
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
                text = stringResource(Res.string.your_share, expense.myAmount(group).format()),
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

@Composable
private fun LentString(
    group: Group,
    expense: Expense,
) {
    if (expense.undistributedAmount != null && expense.undistributedAmount?.value != 0.0) {
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
                text = stringResource(Res.string.not_split_amount, "${undistributed?.format()}"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    } else if (expense.status == ExpenseStatus.SETTLED) {
        Text(
            text = stringResource(Res.string.settled),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    } else if (expense.myAmount(group).value == 0.0 && !expense.payedBy.isMe(group)) {
        Text(
            text = stringResource(Res.string.not_participating),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    } else if (expense.payedBy.isMe(group)) {
        val lent =
            Amount(
                value = expense.totalAmount.value - expense.myAmount(group).value,
                currencyCode = expense.totalAmount.currencyCode,
            )
        if (lent.value != 0.0) {
            Text(
                text = stringResource(Res.string.you_lent, lent.format()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        } else {
            Text(
                text = stringResource(Res.string.personal_expense),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }
    } else {
        Text(
            text = stringResource(Res.string.you_borrowed, expense.myAmount(group).format()),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
internal fun EmptyExpenseSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(1f).padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            modifier = Modifier,
            painter = painterResource(Res.drawable.img_search_empty),
            contentDescription = stringResource(Res.string.empty_transactions_cd),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.empty_transaction_description),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}
