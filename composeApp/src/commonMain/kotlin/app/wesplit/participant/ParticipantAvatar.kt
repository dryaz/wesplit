package app.wesplit.participant

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.group.Participant
import com.seiko.imageloader.model.ImageAction
import com.seiko.imageloader.rememberImageSuccessPainter
import com.seiko.imageloader.ui.AutoSizeBox
import io.github.alexzhirkevich.cupertino.adaptive.icons.AdaptiveIcons
import io.github.alexzhirkevich.cupertino.adaptive.icons.Add
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.ic_user
import split.composeapp.generated.resources.no_image_user

@Composable
fun ParticipantAvatar(
    modifier: Modifier = Modifier,
    participant: Participant,
    size: Dp = 56.dp,
) {
    val photoUrl = participant.user?.photoUrl
    if (photoUrl != null) {
        AutoSizeBox(
            modifier = modifier,
            url = participant.user?.photoUrl ?: "",
        ) { action ->
            when (action) {
                is ImageAction.Success -> {
                    Image(
                        rememberImageSuccessPainter(action),
                        modifier =
                            Modifier
                                .size(size)
                                .clip(CircleShape),
                        contentDescription = participant.name,
                    )
                }

                is ImageAction.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier,
                    )
                }

                is ImageAction.Failure -> {
                    EmptyParticipantAvatar(
                        modifier = Modifier,
                        participant = participant,
                        size = size,
                    )
                }
            }
        }
    } else {
        EmptyParticipantAvatar(
            modifier = modifier,
            participant = participant,
            size = size,
        )
    }
}

@Composable
private fun EmptyParticipantAvatar(
    modifier: Modifier = Modifier,
    participant: Participant,
    size: Dp = 56.dp,
) {
    Box(
        modifier =
            modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerLow),
        contentAlignment = Alignment.Center,
    ) {
        val painter =
            if (participant.user == null) {
                rememberVectorPainter(AdaptiveIcons.Outlined.Add)
            } else {
                painterResource(Res.drawable.ic_user)
            }

        Image(
            modifier = Modifier.size(24.dp),
            painter = painter,
            contentDescription = stringResource(Res.string.no_image_user, participant.name),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
        )
    }
}
