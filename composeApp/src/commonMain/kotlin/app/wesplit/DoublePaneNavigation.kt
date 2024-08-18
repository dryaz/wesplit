package app.wesplit

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.add_user_to_group

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun DoublePaneNavigation(
    secondNavhostEmpty: Boolean,
    menuItems: List<NavigationMenuItem>,
    selectedMenuItem: NavigationMenuItem,
    onMenuItemClick: (NavigationMenuItem) -> Unit,
    firstNavhost: @Composable (Modifier) -> Unit,
    secondNavhost: @Composable (Modifier) -> Unit,
) {
    val windowSizeClass = calculateWindowSizeClass()

    if (windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact) {
        Row(modifier = Modifier.fillMaxHeight(1f)) {
            NavigationRail {
                menuItems.forEach { item ->
                    NavigationRailItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = stringResource(Res.string.add_user_to_group),
                            )
                        },
                        label = { Text(item.title) },
                        selected = item == selectedMenuItem,
                        onClick = { onMenuItemClick(item) },
                    )
                }
            }
            firstNavhost(Modifier.width(360.dp).fillMaxHeight(1f))
            Spacer(modifier = Modifier.fillMaxHeight(1f).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
            // TODO: Calculate weight based on current width, if width is that it will be collapsed -> weight already should be 1.
            //  So it should be from 1 until 2 based on scnreen width. calculateWindowSizeClass() doesn't provide value :(
            //  https://github.com/dryaz/wesplit/issues/15
            secondNavhost(Modifier.weight(1f).fillMaxHeight(1f))
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(1f)) {
            AnimatedVisibility(
                visible = secondNavhostEmpty,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                firstNavhost(Modifier.fillMaxSize(1f))
            }

            AnimatedVisibility(
                visible = !secondNavhostEmpty,
                enter = slideInHorizontally { width -> width * 2 },
                exit = slideOutHorizontally { width -> width * 2 },
            ) {
                secondNavhost(Modifier.fillMaxSize(1f))
            }
        }
    }
}

interface NavigationMenuItem {
    val title: String
}
