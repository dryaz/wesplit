package app.wesplit.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.group.Group
import app.wesplit.domain.model.group.Participant
import app.wesplit.domain.model.group.uiTitle
import app.wesplit.domain.model.user.User
import app.wesplit.participant.ParticipantAvatar
import app.wesplit.theme.AppTheme
import kotlin.random.Random

/**
 * KMP doesn't have proper preview but android do have
 * preview mode + liveedit. So sometimes it makes sense
 * to use playground to build view.
 */
@Composable
@Preview(showSystemUi = true)
fun Playground() =
    AppTheme {
        GroupHeader(
            group =
                Group(
                    id = "1",
                    title = "Awesome group",
                    participants =
                        setOf(
                            Participant(name = "User 1", isMe = Random.nextFloat() > 0.5f),
                            Participant(name = "User 2"),
                            Participant(name = "User 3", user = User("1", "a", "a")),
                            Participant(name = "User 4", user = User("1", "a", "a")),
                        ),
                ),
        )
    }

@Composable
private fun GroupHeader(group: Group) {
    Row(
        modifier = Modifier.padding(16.dp).fillMaxWidth(1f),
    ) {
        Column(
            modifier = Modifier.weight(1f),
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
        Icon(
            Icons.Filled.Share,
            contentDescription = "stringResource(Res.string.share_group)",
        )
    }
}
