package app.wesplit.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import app.wesplit.domain.model.AnalyticsManager
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveTheme
import io.github.alexzhirkevich.cupertino.adaptive.CupertinoThemeSpec
import io.github.alexzhirkevich.cupertino.adaptive.ExperimentalAdaptiveApi
import io.github.alexzhirkevich.cupertino.adaptive.MaterialThemeSpec
import io.github.alexzhirkevich.cupertino.adaptive.Theme
import org.koin.compose.koinInject

@Immutable
data class ColorFamily(
    val color: Color,
    val onColor: Color,
    val colorContainer: Color,
    val onColorContainer: Color,
)

@Immutable
data class ExtraColorsPalette(
    val warning: Color = Color.Unspecified,
    val infoContainer: Color = Color.Unspecified,
    val onInfoContainer: Color = Color.Unspecified,
    val onInfoContainerAction: Color = Color.Unspecified,
)

val LightCustomColorsPalette =
    ExtraColorsPalette(
        warning = warningLight,
        infoContainer = infoContainerLight,
        onInfoContainer = onInfoContainerLight,
        onInfoContainerAction = warningDark,
    )

val DarkCustomColorsPalette =
    ExtraColorsPalette(
        warning = warningDark,
        infoContainer = infoContainerDark,
        onInfoContainer = onInfoContainerDark,
        onInfoContainerAction = warningDark,
    )

val unspecified_scheme =
    ColorFamily(
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
    )

sealed interface ThemeAction {
    data class ChangeTheme(val theme: Theme) : ThemeAction

    data class ChangeColorMode(val mode: ColorMode) : ThemeAction
}

enum class ColorMode {
    LIGHT,
    DARK,
    SYSTEM,
}

@Stable
data class ThemeState(
    val theme: Theme = Theme.Material3,
    val colorMode: ColorMode = ColorMode.SYSTEM,
    val actionCallback: (ThemeAction) -> Unit = {},
)

val LocalThemeState = compositionLocalOf { ThemeState() }
val LocalExtraColorsPalette = staticCompositionLocalOf { ExtraColorsPalette() }

val MaterialTheme.extraColorScheme: ExtraColorsPalette
    @Composable
    @ReadOnlyComposable
    get() = LocalExtraColorsPalette.current

private const val CHANGE_COLOR_EVENT = "change_color_mode"
private const val CHANGE_COLOR_PARAM = "mode"

@OptIn(ExperimentalAdaptiveApi::class)
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val systemDark = isSystemInDarkTheme()
    val analyticsManager: AnalyticsManager = koinInject()

    var colorMode by remember { mutableStateOf(ColorMode.SYSTEM) }
    val darkTheme by remember {
        derivedStateOf {
            colorMode == ColorMode.DARK || (colorMode == ColorMode.SYSTEM && systemDark)
        }
    }

    var theme by remember { mutableStateOf(Theme.Material3) }

    val cupertinoColorScheme =
        when {
            darkTheme -> io.github.alexzhirkevich.cupertino.theme.darkColorScheme()
            else -> io.github.alexzhirkevich.cupertino.theme.lightColorScheme()
        }

    val extraColorsPalette =
        when {
            darkTheme -> DarkCustomColorsPalette
            else -> LightCustomColorsPalette
        }

    AdaptiveTheme(
        material =
        MaterialThemeSpec.Default(
            colorScheme =
            dynamicColorScheme(
                seedColor = Color(0xFF48B04A),
                isDark = darkTheme,
                isAmoled = false,
                style = PaletteStyle.Rainbow,
                modifyColorScheme = { scheme ->
                    if (darkTheme) {
                        scheme.copy(
                            surfaceContainerLowest = scheme.surfaceContainerHighest,
                            surfaceContainerLow = scheme.surfaceContainerHigh,
                            surfaceContainerHigh = scheme.surfaceContainerLow,
                            surfaceContainerHighest = scheme.surfaceContainerLowest,
                        )
                    } else {
                        scheme
                    }
                },
            ),
            typography = MaterialTypography(),
        ),
        cupertino =
        CupertinoThemeSpec.Default(
            colorScheme = cupertinoColorScheme,
        ),
        content = {
            val themeState =
                ThemeState(
                    theme = theme,
                    colorMode = colorMode,
                    actionCallback = { action ->
                        when (action) {
                            is ThemeAction.ChangeColorMode -> {
                                analyticsManager.track(
                                    CHANGE_COLOR_EVENT, mapOf(
                                        CHANGE_COLOR_PARAM to action.mode.toString()
                                    )
                                )
                                colorMode = action.mode
                            }

                            is ThemeAction.ChangeTheme -> theme = action.theme
                        }
                    },
                )

            CompositionLocalProvider(
                LocalExtraColorsPalette provides extraColorsPalette,
                LocalThemeState provides themeState,
            ) {
                content()
            }
        },
    )
}
