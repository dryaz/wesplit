package app.wesplit.participant

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.group.Participant
import app.wesplit.domain.model.group.isMe
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.new
import split.composeapp.generated.resources.you

@Composable
fun ParticipantListItem(
    modifier: Modifier = Modifier,
    participant: Participant,
    enabled: Boolean = true,
    showImage: Boolean = true,
    showMeBadge: Boolean = true,
    avatarSize: Dp = 56.dp,
    subComposable: @Composable (() -> Unit)? = null,
    action: @Composable (() -> Unit)? = null,
    onClick: ((Participant) -> Unit)? = null,
) {
    val internalModifier =
        if (onClick != null && enabled) {
            modifier.clickable { onClick(participant) }
        } else {
            modifier
        }

    Row(
        modifier =
            internalModifier
                .fillMaxWidth(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showImage) {
            ParticipantAvatar(
                participant = participant,
                size = avatarSize,
            )
        }

        val suffix =
            if (participant.isMe()) {
                " (${stringResource(Res.string.you)})"
            } else if (participant.user == null) {
                " (${stringResource(Res.string.new)})"
            } else {
                ""
            }

        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = participant.name + if (showMeBadge) suffix else "",
            )
            subComposable?.let { it() }
        }
        action?.let {
            it()
        }
    }
}
