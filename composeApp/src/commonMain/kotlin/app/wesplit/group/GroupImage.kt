package app.wesplit.group

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveCircularProgressIndicator
import io.github.alexzhirkevich.cupertino.adaptive.ExperimentalAdaptiveApi
import org.jetbrains.compose.resources.painterResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.ic_group
import split.composeapp.generated.resources.ic_plus_round

@OptIn(ExperimentalAdaptiveApi::class)
@Composable
fun GroupImage(
    modifier: Modifier = Modifier,
    imageUrl: String?,
    groupTitle: String,
    isLoading: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    AutoSizeBox(
        modifier =
            modifier.size(52.dp).then(
                onClick?.let { Modifier.clickable { it() } } ?: Modifier,
            ),
        url = imageUrl ?: "",
    ) { action ->
        if (isLoading) {
            AdaptiveCircularProgressIndicator()
        } else {
            when (action) {
                is ImageAction.Success -> {
                    Image(
                        rememberImageSuccessPainter(action),
                        modifier = Modifier.size(52.dp).clip(CircleShape),
                        contentDescription = groupTitle,
                    )
                }

                is ImageAction.Loading -> {
                    AdaptiveCircularProgressIndicator()
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
        }

        onClick?.let {
            Box(
                modifier = Modifier.fillMaxSize(1f),
                contentAlignment = Alignment.TopEnd,
            ) {
                Image(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(Res.drawable.ic_plus_round),
                    contentDescription = "Add group image",
                )
            }
        }
    }
}
