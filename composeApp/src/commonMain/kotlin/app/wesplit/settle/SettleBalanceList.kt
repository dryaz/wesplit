package app.wesplit.settle

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.currency.format
import app.wesplit.domain.model.group.Balance
import app.wesplit.domain.settle.SettleSuggestion
import app.wesplit.participant.ParticipantListItem
import org.jetbrains.compose.resources.painterResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.ic_flag

@OptIn(ExperimentalLayoutApi::class)
@Composable
@Deprecated("Need to be refactored, improved")
fun SettleBalanceList(
    balance: Balance?,
    appliedSuggestions: List<SettleSuggestion>?,
    footer: @Composable () -> Unit,
) {
    if (balance != null) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth(1f)
                    .padding(16.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
        ) {
            AnimatedVisibility(visible = appliedSuggestions.isNullOrEmpty()) {
                Column {
                    TotalBalances(balance)

                    if (balance.undistributed.isNotEmpty()) {
                        HorizontalDivider()
                        Undistributed(balance)
                    }
                }
            }

            AnimatedVisibility(visible = !appliedSuggestions.isNullOrEmpty()) {
                Column {
                    appliedSuggestions?.let {
                        SuggestedPayouts(it)
                    } ?: TotalBalances(balance)
                }
            }

            HorizontalDivider()
            footer()
        }
    } else {
        SettledBalances()
    }
}

@Composable
private fun SuggestedPayouts(balance: List<SettleSuggestion>) {
    val suggestions = balance.groupBy { it.payer }
    suggestions.forEach { entry ->
        val payer = entry.key
        if (payer != null) {
            ParticipantListItem(
                participant = payer,
                subComposable = {
                    Text(
                        text = "Should pay to",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                    )
                },
            )
        } else {
            ListItem(
                colors =
                    ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    ),
                headlineContent = {
                    Text(
                        text = "Undistributed",
                    )
                },
                leadingContent = {
                    Icon(
                        modifier = Modifier.width(56.dp),
                        painter = painterResource(Res.drawable.ic_flag),
                        contentDescription = "Undistributed",
                    )
                },
                supportingContent = {
                    Text(
                        text = "Time to forgive and forget?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                    )
                },
            )
        }

        entry.value.forEachIndexed { index, suggestion ->
            Row(
                modifier = Modifier.height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(Modifier.width(42.dp))
                Spacer(Modifier.width(2.dp).fillMaxHeight(1f).background(MaterialTheme.colorScheme.outlineVariant))
                Spacer(Modifier.width(18.dp).height(2.dp).background(MaterialTheme.colorScheme.outlineVariant))
                ParticipantListItem(
                    participant = suggestion.recipient,
                    avatarSize = 40.dp,
                    subComposable = {
                        Text(
                            text = "Gets back",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    },
                    action = {
                        Text(
                            text = "${suggestion.amount.format()}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    },
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun TotalBalances(balance: Balance) {
    balance.participantsBalance.forEach { balanceItem ->
        ParticipantListItem(
            participant = balanceItem.participant,
            subComposable = {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    balanceItem.amounts.filter { it.value != 0.0 }.forEach { amount ->
                        FilterChip(
                            selected = false,
                            onClick = {},
                            enabled = false,
                            label = { Text(amount.format()) },
                            colors =
                                FilterChipDefaults.filterChipColors(
                                    disabledContainerColor =
                                        if (amount.value > 0.0) {
                                            MaterialTheme.colorScheme.secondaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.error
                                        },
                                    disabledLabelColor =
                                        if (amount.value > 0.0) {
                                            MaterialTheme.colorScheme.onSecondaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onError
                                        },
                                ),
                        )
                    }
                }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Undistributed(balance: Balance) {
    ListItem(
        colors =
            ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            ),
        headlineContent = {
            Text(
                text = "Undistributed",
            )
        },
        leadingContent = {
            Icon(
                modifier = Modifier.width(56.dp),
                painter = painterResource(Res.drawable.ic_flag),
                contentDescription = "Undistributed",
            )
        },
        supportingContent = {
            FlowRow(
                modifier = Modifier.fillMaxWidth(1f),
            ) {
                balance.undistributed.forEach { amount ->
                    SuggestionChip(
                        onClick = {},
                        label = { Text(amount.format()) },
                    )
                }
            }
        },
    )
}

@Composable
private fun SettledBalances() {
    Text("Settled balances here")
}
