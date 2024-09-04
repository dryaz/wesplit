package app.wesplit.group

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.group.Participant
import com.seiko.imageloader.model.ImageAction
import com.seiko.imageloader.rememberImageSuccessPainter
import com.seiko.imageloader.ui.AutoSizeBox
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.ic_user
import split.composeapp.generated.resources.new
import split.composeapp.generated.resources.you

@Composable
fun ParticipantListItem(
    participant: Participant,
    action: @Composable (() -> Unit)? = null,
    onClick: ((Participant) -> Unit)? = null,
) {
    val modifier = onClick?.let { Modifier.clickable { onClick(participant) } } ?: Modifier
    Row(
        modifier =
            modifier
                .fillMaxWidth(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val photoUrl = participant.user?.photoUrl
        if (photoUrl != null) {
            AutoSizeBox(
                url = participant.user?.photoUrl ?: "",
            ) { action ->
                when (action) {
                    is ImageAction.Success -> {
                        Image(
                            rememberImageSuccessPainter(action),
                            modifier = Modifier.size(56.dp).clip(CircleShape),
                            contentDescription = participant.name,
                        )
                    }

                    is ImageAction.Loading -> {
                        CircularProgressIndicator()
                    }

                    is ImageAction.Failure -> {
                        EmptyImage(participant)
                    }
                }
            }
        } else {
            EmptyImage(participant)
        }

        val suffix =
            if (participant.isMe) {
                " (${stringResource(Res.string.you)})"
            } else if (participant.id == null) {
                " (${stringResource(Res.string.new)})"
            } else {
                ""
            }

        Spacer(modifier = Modifier.width(16.dp))
        Text(
            modifier = Modifier.weight(1f),
            text = participant.name + suffix,
        )
        action?.let {
            it()
        }
    }
}

@Composable
private fun EmptyImage(participant: Participant) {
    Box(
        modifier = Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerLow),
        contentAlignment = Alignment.Center,
    ) {
        val painter =
            if (participant.id == null) {
                rememberVectorPainter(Icons.Filled.Add)
            } else {
                painterResource(Res.drawable.ic_user)
            }

        Image(
            modifier = Modifier.size(24.dp),
            painter = painter,
            contentDescription = "No image for user ${participant.name}",
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
        )
    }
}
