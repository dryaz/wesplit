package app.wesplit.ui.molecules

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.group.Participant
import app.wesplit.participant.ParticipantAvatar

@Composable
fun ParticipantAvatars(
    modifier: Modifier = Modifier,
    addIconEnabled: Boolean = true,
    participants: Collection<Participant>,
) {
    Box(modifier = modifier) {
        participants.forEachIndexed { index, participant ->
            ParticipantAvatar(
                modifier = Modifier.padding(start = 20.dp * index),
                addIconEnabled = addIconEnabled,
                participant = participant,
                size = 36.dp,
            )
        }
    }
}
