package app.wesplit.group.detailed.balance

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.expense.format
import app.wesplit.domain.model.group.balance.Balance
import app.wesplit.participant.ParticipantListItem
import kotlin.math.roundToInt

@Composable
fun BalanceList(balance: Balance?) {
    if (balance != null) {
        Card(
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
            modifier =
                Modifier
                    .widthIn(max = 450.dp)
                    .fillMaxWidth(1f)
                    .padding(16.dp),
        ) {
            balance.participants.forEach { participantItem ->
                ParticipantListItem(
                    participant = participantItem.key,
                    action = {
                        Text(participantItem.value.balance.format())
                    },
                )
            }

            val nonDistr = (balance.nonDistributed.value * 100f).roundToInt() / 100f
            if (nonDistr != 0.0f) {
                HorizontalDivider(modifier = Modifier.fillMaxWidth(1f))
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth(1f)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = "Undistributed",
                    )

                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Undistributed",
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = balance.nonDistributed.format(),
                    )
                }
            }
        }
    } else {
        Text("Empty balances")
    }
}
