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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import app.wesplit.theme.ColorMode
import app.wesplit.theme.LocalThemeState
import app.wesplit.theme.ThemeAction
import io.github.alexzhirkevich.cupertino.adaptive.Theme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.app_name
import split.composeapp.generated.resources.change_color_mode
import split.composeapp.generated.resources.change_theme
import split.composeapp.generated.resources.ic_android
import split.composeapp.generated.resources.ic_dark
import split.composeapp.generated.resources.ic_light
import split.composeapp.generated.resources.ic_system
import split.composeapp.generated.resources.ic_web

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
        Row(
            modifier =
                Modifier
                    .fillMaxHeight(1f),
        ) {
            // TODO: https://m3.material.io/ has navigation rail only on expanded but even not medium
            NavigationRail(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                AppLogo(modifier = Modifier.size(56.dp))
                Spacer(modifier = Modifier.height(8.dp))
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
                Spacer(modifier = Modifier.weight(1f))

                val localTheme = LocalThemeState.current

                NavigationRailItem(
                    icon = {
                        Icon(
                            painter =
                                painterResource(
                                    when (localTheme.theme) {
                                        Theme.Cupertino -> Res.drawable.ic_web
                                        Theme.Material3 -> Res.drawable.ic_android
                                    },
                                ),
                            contentDescription = stringResource(Res.string.change_theme),
                        )
                    },
                    selected = false,
                    onClick = {
                        when (localTheme.theme) {
                            Theme.Cupertino -> localTheme.actionCallback(ThemeAction.ChangeTheme(Theme.Material3))
                            Theme.Material3 -> localTheme.actionCallback(ThemeAction.ChangeTheme(Theme.Cupertino))
                        }
                    },
                )

                NavigationRailItem(
                    icon = {
                        Icon(
                            painter =
                                painterResource(
                                    when (localTheme.colorMode) {
                                        ColorMode.LIGHT -> Res.drawable.ic_light
                                        ColorMode.DARK -> Res.drawable.ic_dark
                                        ColorMode.SYSTEM -> Res.drawable.ic_system
                                    },
                                ),
                            contentDescription = stringResource(Res.string.change_color_mode),
                        )
                    },
                    selected = false,
                    onClick = {
                        when (localTheme.colorMode) {
                            ColorMode.LIGHT -> localTheme.actionCallback(ThemeAction.ChangeColorMode(ColorMode.DARK))
                            ColorMode.DARK -> localTheme.actionCallback(ThemeAction.ChangeColorMode(ColorMode.SYSTEM))
                            ColorMode.SYSTEM -> localTheme.actionCallback(ThemeAction.ChangeColorMode(ColorMode.LIGHT))
                        }
                    },
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
            Row(
                modifier =
                    Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(20.dp)),
            ) {
                firstNavhost(Modifier.width(320.dp).fillMaxHeight(1f))
                VerticalDivider()
                // TODO: Calculate weight based on current width, if width is that it will be collapsed -> weight already should be 1.
                //  So it should be from 1 until 2 based on scnreen width. calculateWindowSizeClass() doesn't provide value :(
                //  https://github.com/dryaz/wesplit/issues/15
                secondNavhost(Modifier.weight(1f).fillMaxHeight(1f))
            }
        }
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Row(modifier = Modifier.fillMaxWidth(1f)) {
                        AppLogo(modifier = Modifier.size(56.dp))
                        Text(
                            text = stringResource(Res.string.app_name),
                            modifier = Modifier.padding(16.dp),
                        )
                    }
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
            Box(
                modifier =
                    Modifier
                        .fillMaxSize(1f)
                        .background(MaterialTheme.colorScheme.surface),
            ) {
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
