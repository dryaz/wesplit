package app.wesplit.group

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.seiko.imageloader.model.ImageAction
import com.seiko.imageloader.rememberImageSuccessPainter
import com.seiko.imageloader.ui.AutoSizeBox
import io.github.alexzhirkevich.cupertino.adaptive.icons.AdaptiveIcons
import io.github.alexzhirkevich.cupertino.adaptive.icons.AddCircle
import org.jetbrains.compose.resources.painterResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.ic_group

@Composable
fun GroupImage(
    modifier: Modifier = Modifier,
    imageUrl: String?,
    groupTitle: String,
    onClick: (() -> Unit)? = null,
) {
    AutoSizeBox(
        modifier =
            modifier.size(52.dp).then(
                onClick?.let { Modifier.clickable { it() } } ?: Modifier,
            ),
        url = imageUrl ?: "",
    ) { action ->
        when (action) {
            is ImageAction.Success -> {
                Image(
                    rememberImageSuccessPainter(action),
                    modifier = Modifier.size(52.dp).clip(CircleShape),
                    contentDescription = groupTitle,
                )
            }

            is ImageAction.Loading -> {
                CircularProgressIndicator()
            }

            is ImageAction.Failure -> {
                Box(
                    modifier =
                        Modifier.size(52.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerLow),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(Res.drawable.ic_group),
                        contentDescription = "No image for user $groupTitle",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                    )
                }
            }
        }

        onClick?.let {
            Box(
                modifier = Modifier.fillMaxSize(1f),
                contentAlignment = Alignment.TopEnd,
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = AdaptiveIcons.Outlined.AddCircle,
                    contentDescription = "Add group image",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
