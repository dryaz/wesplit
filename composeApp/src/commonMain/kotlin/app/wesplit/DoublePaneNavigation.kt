package app.wesplit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun DoublePaneNavigation(
    secondNavhostEmpty: Boolean,
    menuItems: List<NavigationMenuItem>,
    selectedMenuItem: NavigationMenuItem,
    onMenuItemClick: (NavigationMenuItem) -> Unit,
    drawerState: DrawerState,
    firstNavhost: @Composable (Modifier) -> Unit,
    secondNavhost: @Composable (Modifier) -> Unit,
) {
    val windowSizeClass = calculateWindowSizeClass()

    if (windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact) {
        Row(modifier = Modifier.fillMaxHeight(1f)) {
            // TODO: https://m3.material.io/ has navigation rail only on expanded but even not medium
            NavigationRail {
                menuItems.forEach { item ->
                    NavigationRailItem(
                        icon = {
                            Icon(
                                painter = painterResource(item.icon),
                                contentDescription = stringResource(item.title),
                            )
                        },
                        label = { Text(stringResource(item.title)) },
                        selected = item == selectedMenuItem,
                        onClick = { onMenuItemClick(item) },
                    )
                }
            }
            firstNavhost(Modifier.width(320.dp).fillMaxHeight(1f))
            VerticalDivider()
            // TODO: Calculate weight based on current width, if width is that it will be collapsed -> weight already should be 1.
            //  So it should be from 1 until 2 based on scnreen width. calculateWindowSizeClass() doesn't provide value :(
            //  https://github.com/dryaz/wesplit/issues/15
            secondNavhost(Modifier.weight(1f).fillMaxHeight(1f))
        }
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Text("Drawer title", modifier = Modifier.padding(16.dp))
                    HorizontalDivider()
                    menuItems.forEach { item ->
                        NavigationDrawerItem(
                            label = { Text(text = stringResource(item.title)) },
                            selected = item == selectedMenuItem,
                            onClick = { onMenuItemClick(item) },
                            shape = RectangleShape,
                            icon = {
                                Icon(
                                    painter = painterResource(item.icon),
                                    contentDescription = stringResource(item.title),
                                )
                            },
                        )
                    }
                }
            },
        ) {
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
}

interface NavigationMenuItem {
    val title: StringResource
    val icon: DrawableResource
}
