package app.wesplit.ui.molecules

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.group.Participant
import app.wesplit.participant.ParticipantAvatar

@Composable
fun ParticipantAvatars(
    modifier: Modifier = Modifier,
    addIconEnabled: Boolean = true,
    participants: Collection<Participant>,
    size: Dp = 36.dp,
) {
    Box(modifier = modifier) {
        participants.forEachIndexed { index, participant ->
            ParticipantAvatar(
                modifier = Modifier.padding(start = (size.div(1.8f)) * index),
                addIconEnabled = addIconEnabled,
                participant = participant,
                size = size,
            )
        }
    }
}
