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
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerState
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
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.KotlinPlatform
import app.wesplit.domain.model.currentPlatform
import app.wesplit.theme.ColorMode
import app.wesplit.theme.LocalThemeState
import app.wesplit.theme.ThemeAction
import io.github.alexzhirkevich.cupertino.adaptive.Theme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.app_version
import split.composeapp.generated.resources.change_color_mode
import split.composeapp.generated.resources.change_theme
import split.composeapp.generated.resources.ic_android
import split.composeapp.generated.resources.ic_dark
import split.composeapp.generated.resources.ic_feedback
import split.composeapp.generated.resources.ic_light
import split.composeapp.generated.resources.ic_mobile_app
import split.composeapp.generated.resources.ic_system
import split.composeapp.generated.resources.ic_web
import split.composeapp.generated.resources.submit_feedback_cd

// TODO: Add expense from top lvl view without defining the group
//  https://github.com/dryaz/wesplit/issues/68
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
    // TODO: AB test rather to send email or fill the form via prodcamp
    val uriHandler = LocalUriHandler.current
    val shareDelegate: ShareDelegate = koinInject()

    if (windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact) {
        Row(
            modifier =
                Modifier.fillMaxSize(1f).background(MaterialTheme.colorScheme.surfaceContainerHighest).displayCutoutPadding()
                    .imePadding().navigationBarsPadding(),
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

                // TODO: Leave for debug only
                if (false) {
                    NavigationRailItem(icon = {
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
                    }, selected = false, onClick = {
                        when (localTheme.theme) {
                            Theme.Cupertino -> localTheme.actionCallback(ThemeAction.ChangeTheme(Theme.Material3))
                            Theme.Material3 -> localTheme.actionCallback(ThemeAction.ChangeTheme(Theme.Cupertino))
                        }
                    })
                }

                if (currentPlatform !is KotlinPlatform.Mobile) {
                    NavigationRailItem(icon = {
                        Icon(
                            painter = painterResource(Res.drawable.ic_mobile_app),
                            contentDescription = "Get mobile application",
                        )
                    }, selected = false, onClick = {
                        if (shareDelegate.supportPlatformSharing()) {
                            shareDelegate.open(ShareData.Link("https://wesplit.app"))
                        } else {
                            uriHandler.openUri("https://wesplit.app")
                        }
                    }, label = {
                        Text(
                            text = "Get App",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    })
                }

                NavigationRailItem(icon = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_feedback),
                        contentDescription = stringResource(Res.string.submit_feedback_cd),
                    )
                }, selected = false, onClick = {
                    if (shareDelegate.supportPlatformSharing()) {
                        shareDelegate.open(ShareData.Link("https://wesplit.prodcamp.com/"))
                    } else {
                        uriHandler.openUri("https://wesplit.prodcamp.com/")
                    }
                }, label = {
                    Text(
                        text = "Feedback",
                        style = MaterialTheme.typography.labelSmall,
                    )
                })

                NavigationRailItem(icon = {
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
                }, label = {
                    Text(
                        text = localTheme.colorMode.name.lowercase().capitalize(Locale.current),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }, selected = false, onClick = {
                    when (localTheme.colorMode) {
                        ColorMode.LIGHT -> localTheme.actionCallback(ThemeAction.ChangeColorMode(ColorMode.DARK))
                        ColorMode.DARK -> localTheme.actionCallback(ThemeAction.ChangeColorMode(ColorMode.SYSTEM))
                        ColorMode.SYSTEM -> localTheme.actionCallback(ThemeAction.ChangeColorMode(ColorMode.LIGHT))
                    }
                })

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(Res.string.app_version),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            Row(
                modifier =
                    Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest).padding(16.dp)
                        .clip(RoundedCornerShape(20.dp)),
            ) {
                val width = if (windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact) 220.dp else 320.dp
                firstNavhost(Modifier.width(width).fillMaxHeight(1f))
                VerticalDivider()
                // TODO: Calculate weight based on current width, if width is that it will be collapsed -> weight already should be 1.
                //  So it should be from 1 until 2 based on scnreen width. calculateWindowSizeClass() doesn't provide value :(
                //  https://github.com/dryaz/wesplit/issues/15
                secondNavhost(Modifier.weight(1f).fillMaxHeight(1f))
            }
        }
    } else {
        ModalNavigationDrawer(drawerState = drawerState, drawerContent = {
            ModalDrawerSheet {
                Box(
                    modifier = Modifier.fillMaxWidth(1f).padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    AppLogo(modifier = Modifier.size(56.dp))
                }

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

                Spacer(modifier = Modifier.weight(1f))

                if (currentPlatform !is KotlinPlatform.Mobile) {
                    NavigationDrawerItem(
                        label = { Text(text = "Get mobile app") },
                        selected = false,
                        onClick = {
                            if (shareDelegate.supportPlatformSharing()) {
                                shareDelegate.open(ShareData.Link("https://wesplit.app"))
                            } else {
                                uriHandler.openUri("https://wesplit.app")
                            }
                        },
                        shape = RectangleShape,
                        icon = {
                            Icon(
                                painter = painterResource(Res.drawable.ic_mobile_app),
                                contentDescription = stringResource(Res.string.submit_feedback_cd),
                            )
                        },
                    )
                }

                NavigationDrawerItem(
                    label = { Text(text = stringResource(Res.string.submit_feedback_cd)) },
                    selected = false,
                    onClick = {
                        if (shareDelegate.supportPlatformSharing()) {
                            shareDelegate.open(ShareData.Link("https://wesplit.prodcamp.com/"))
                        } else {
                            uriHandler.openUri("https://wesplit.prodcamp.com/")
                        }
                    },
                    shape = RectangleShape,
                    icon = {
                        Icon(
                            painter = painterResource(Res.drawable.ic_feedback),
                            contentDescription = stringResource(Res.string.submit_feedback_cd),
                        )
                    },
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    modifier = Modifier.fillMaxWidth(1f),
                    text = stringResource(Res.string.app_version),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }) {
            Box(
                modifier = Modifier.fillMaxSize(1f).background(MaterialTheme.colorScheme.surface).imePadding(),
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
