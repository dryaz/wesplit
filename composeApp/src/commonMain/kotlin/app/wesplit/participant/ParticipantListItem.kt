package app.wesplit.participant

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.group.Participant
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.new
import split.composeapp.generated.resources.you

@Composable
fun ParticipantListItem(
    participant: Participant,
    modifier: Modifier = Modifier,
    subTitle: String? = null,
    action: @Composable (() -> Unit)? = null,
    onClick: ((Participant) -> Unit)? = null,
) {
    val internalModifier = onClick?.let { modifier.clickable { onClick(participant) } } ?: modifier
    Row(
        modifier =
            internalModifier
                .fillMaxWidth(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ParticipantAvatar(participant = participant)
        val suffix =
            if (participant.isMe) {
                " (${stringResource(Res.string.you)})"
            } else if (participant.id == null) {
                " (${stringResource(Res.string.new)})"
            } else {
                ""
            }

        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = participant.name + suffix,
            )
            subTitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
        action?.let {
            it()
        }
    }
}
