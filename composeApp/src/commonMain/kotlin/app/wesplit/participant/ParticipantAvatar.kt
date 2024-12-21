package app.wesplit.participant

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.wesplit.domain.model.group.Participant
import com.seiko.imageloader.model.ImageAction
import com.seiko.imageloader.rememberImageSuccessPainter
import com.seiko.imageloader.ui.AutoSizeBox
import io.github.alexzhirkevich.cupertino.adaptive.icons.AdaptiveIcons
import io.github.alexzhirkevich.cupertino.adaptive.icons.Add
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.no_image_user
import kotlin.math.abs
import kotlin.random.Random

private const val MIN_COLOR = 95
private const val MAX_COLOR = 195

@Composable
fun ParticipantAvatar(
    modifier: Modifier = Modifier,
    participant: Participant,
    addIconEnabled: Boolean = true,
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
                        addIconEnabled = addIconEnabled,
                        participant = participant,
                        size = size,
                    )
                }
            }
        }
    } else {
        EmptyParticipantAvatar(
            modifier = modifier,
            addIconEnabled = addIconEnabled,
            participant = participant,
            size = size,
        )
    }
}

@Composable
private fun EmptyParticipantAvatar(
    modifier: Modifier = Modifier,
    addIconEnabled: Boolean = true,
    participant: Participant,
    size: Dp = 56.dp,
) {
    val bgColor =
        remember(participant) {
            getRandomColorFromString(participant.id)
        }
    Box(
        modifier =
            modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    if (addIconEnabled && participant.user == null) {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    } else {
                        bgColor
                    },
                ),
        contentAlignment = Alignment.Center,
    ) {
        if (participant.user == null && addIconEnabled) {
            Image(
                modifier = Modifier.size(24.dp.div(44.dp.div(size))),
                painter = rememberVectorPainter(AdaptiveIcons.Outlined.Add),
                contentDescription = stringResource(Res.string.no_image_user, participant.name),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
            )
        } else {
            Text(
                text = participant.name.first().uppercase(),
                fontSize = 24.sp.div(44.dp.div(size)),
                color = if (bgColor.luminance() > 0.5) Color.Black else Color.White,
            )
        }
    }
}

private fun getRandomColorFromString(input: String): Color {
    val hash = input.hashCode()
    val random = Random(abs(hash))
    val red = random.nextInt(MIN_COLOR, MAX_COLOR)
    val green = random.nextInt(MIN_COLOR, MAX_COLOR)
    val blue = random.nextInt(MIN_COLOR, MAX_COLOR)
    return Color(red, green, blue)
}

private fun isColorDark(color: Color): Boolean {
    val darkness = 1 - (0.299 * color.red + 0.587 * color.green + 0.114 * color.blue)
    return darkness >= 0.5
}
