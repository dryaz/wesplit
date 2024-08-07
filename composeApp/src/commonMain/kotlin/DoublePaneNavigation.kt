import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun DoublePaneNavigation(
    secondNavhostEmpty: Boolean,
    firstNavhost: @Composable (Modifier) -> Unit,
    secondNavhost: @Composable (Modifier) -> Unit
) {
    val windowSizeClass = calculateWindowSizeClass()

    if (windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact) {
        Row(modifier = Modifier.fillMaxHeight(1f)) {
            firstNavhost(Modifier.weight(1f).fillMaxHeight(1f))
            Spacer(modifier = Modifier.fillMaxHeight(1f).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
            secondNavhost(Modifier.weight(2f).fillMaxHeight(1f))
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(1f)) {
            AnimatedVisibility(
                visible = secondNavhostEmpty,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                firstNavhost(Modifier.fillMaxSize(1f))
            }

            AnimatedVisibility(
                visible = !secondNavhostEmpty,
                enter = slideInHorizontally { width -> width * 2 },
                exit = slideOutHorizontally { width -> width * 2 }
            ) {
                secondNavhost(Modifier.fillMaxSize(1f))
            }
        }
    }
}
