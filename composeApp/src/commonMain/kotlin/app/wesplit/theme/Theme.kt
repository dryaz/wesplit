package app.wesplit.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveTheme
import io.github.alexzhirkevich.cupertino.adaptive.CupertinoThemeSpec
import io.github.alexzhirkevich.cupertino.adaptive.ExperimentalAdaptiveApi
import io.github.alexzhirkevich.cupertino.adaptive.MaterialThemeSpec
import io.github.alexzhirkevich.cupertino.adaptive.Theme

@Immutable
data class ColorFamily(
    val color: Color,
    val onColor: Color,
    val colorContainer: Color,
    val onColorContainer: Color,
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

@OptIn(ExperimentalAdaptiveApi::class)
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val systemDark = isSystemInDarkTheme()

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

    AdaptiveTheme(
        target = theme,
        material =
            MaterialThemeSpec.Default(
                colorScheme =
                    dynamicColorScheme(
                        seedColor = Color(0xFF456731),
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
                            is ThemeAction.ChangeColorMode -> colorMode = action.mode
                            is ThemeAction.ChangeTheme -> theme = action.theme
                        }
                    },
                )

            CompositionLocalProvider(LocalThemeState provides themeState) {
                content()
            }
        },
    )
}

private val lightScheme =
    lightColorScheme(
        primary = primaryLight,
        onPrimary = onPrimaryLight,
        primaryContainer = primaryContainerLight,
        onPrimaryContainer = onPrimaryContainerLight,
        secondary = secondaryLight,
        onSecondary = onSecondaryLight,
        secondaryContainer = secondaryContainerLight,
        onSecondaryContainer = onSecondaryContainerLight,
        tertiary = tertiaryLight,
        onTertiary = onTertiaryLight,
        tertiaryContainer = tertiaryContainerLight,
        onTertiaryContainer = onTertiaryContainerLight,
        error = errorLight,
        onError = onErrorLight,
        errorContainer = errorContainerLight,
        onErrorContainer = onErrorContainerLight,
        background = backgroundLight,
        onBackground = onBackgroundLight,
        surface = surfaceLight,
        onSurface = onSurfaceLight,
        surfaceVariant = surfaceVariantLight,
        onSurfaceVariant = onSurfaceVariantLight,
        outline = outlineLight,
        outlineVariant = outlineVariantLight,
        scrim = scrimLight,
        inverseSurface = inverseSurfaceLight,
        inverseOnSurface = inverseOnSurfaceLight,
        inversePrimary = inversePrimaryLight,
        surfaceDim = surfaceDimLight,
        surfaceBright = surfaceBrightLight,
        surfaceContainerLowest = surfaceContainerLowestLight,
        surfaceContainerLow = surfaceContainerLowLight,
        surfaceContainer = surfaceContainerLight,
        surfaceContainerHigh = surfaceContainerHighLight,
        surfaceContainerHighest = surfaceContainerHighestLight,
    )

private val darkScheme =
    darkColorScheme(
        primary = primaryDark,
        onPrimary = onPrimaryDark,
        primaryContainer = primaryContainerDark,
        onPrimaryContainer = onPrimaryContainerDark,
        secondary = secondaryDark,
        onSecondary = onSecondaryDark,
        secondaryContainer = secondaryContainerDark,
        onSecondaryContainer = onSecondaryContainerDark,
        tertiary = tertiaryDark,
        onTertiary = onTertiaryDark,
        tertiaryContainer = tertiaryContainerDark,
        onTertiaryContainer = onTertiaryContainerDark,
        error = errorDark,
        onError = onErrorDark,
        errorContainer = errorContainerDark,
        onErrorContainer = onErrorContainerDark,
        background = backgroundDark,
        onBackground = onBackgroundDark,
        surface = surfaceDark,
        onSurface = onSurfaceDark,
        surfaceVariant = surfaceVariantDark,
        onSurfaceVariant = onSurfaceVariantDark,
        outline = outlineDark,
        outlineVariant = outlineVariantDark,
        scrim = scrimDark,
        inverseSurface = inverseSurfaceDark,
        inverseOnSurface = inverseOnSurfaceDark,
        inversePrimary = inversePrimaryDark,
        surfaceDim = surfaceDimDark,
        surfaceBright = surfaceBrightDark,
        surfaceContainerLowest = surfaceContainerLowestDark,
        surfaceContainerLow = surfaceContainerLowDark,
        surfaceContainer = surfaceContainerDark,
        surfaceContainerHigh = surfaceContainerHighDark,
        surfaceContainerHighest = surfaceContainerHighestDark,
    )
