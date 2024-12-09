package app.wesplit.ui.molecules

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.group.Group
import app.wesplit.domain.model.group.uiTitle
import app.wesplit.participant.ParticipantAvatar

@Composable
fun GroupHead(
    modifier: Modifier = Modifier,
    group: Group,
) {
    Column(
        modifier = modifier,
    ) {
        Text(
            text = group.uiTitle(),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box {
            group.participants.forEachIndexed { index, participant ->
                ParticipantAvatar(
                    modifier = Modifier.padding(start = 20.dp * index),
                    participant = participant,
                    size = 36.dp,
                )
            }
        }
    }
}
