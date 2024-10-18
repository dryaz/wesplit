package app.wesplit.settle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.currency.format
import app.wesplit.domain.model.group.Balance
import app.wesplit.participant.ParticipantListItem
import org.jetbrains.compose.resources.painterResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.ic_flag

@OptIn(ExperimentalLayoutApi::class)
@Composable
@Deprecated("Need to be refactored, improved")
fun SettleBalanceList(
    balance: Balance?,
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

            if (balance.undistributed.isNotEmpty()) {
                HorizontalDivider()
                Undistributed(balance)
            }

            HorizontalDivider()
            footer()
        }
    } else {
        SettledBalances()
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
