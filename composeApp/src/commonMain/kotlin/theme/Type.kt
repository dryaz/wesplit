package theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import split.composeapp.generated.resources.Lexend_Black
import split.composeapp.generated.resources.Lexend_Bold
import split.composeapp.generated.resources.Lexend_ExtraBold
import split.composeapp.generated.resources.Lexend_ExtraLight
import split.composeapp.generated.resources.Lexend_Light
import split.composeapp.generated.resources.Lexend_Medium
import split.composeapp.generated.resources.Lexend_Regular
import split.composeapp.generated.resources.Lexend_SemiBold
import split.composeapp.generated.resources.Lexend_Thin
import split.composeapp.generated.resources.PublicSans_Black
import split.composeapp.generated.resources.PublicSans_Bold
import split.composeapp.generated.resources.PublicSans_ExtraBold
import split.composeapp.generated.resources.PublicSans_ExtraLight
import split.composeapp.generated.resources.PublicSans_Light
import split.composeapp.generated.resources.PublicSans_Medium
import split.composeapp.generated.resources.PublicSans_Regular
import split.composeapp.generated.resources.PublicSans_SemiBold
import split.composeapp.generated.resources.PublicSans_Thin
import split.composeapp.generated.resources.Res

@Composable
fun BodyFontFamily() =
    FontFamily(
        Font(Res.font.PublicSans_Thin, weight = FontWeight.Thin),
        Font(Res.font.PublicSans_ExtraLight, weight = FontWeight.ExtraLight),
        Font(Res.font.PublicSans_Light, weight = FontWeight.Light),
        Font(Res.font.PublicSans_Regular, weight = FontWeight.Normal),
        Font(Res.font.PublicSans_Medium, weight = FontWeight.Medium),
        Font(Res.font.PublicSans_SemiBold, weight = FontWeight.SemiBold),
        Font(Res.font.PublicSans_Bold, weight = FontWeight.Bold),
        Font(Res.font.PublicSans_ExtraBold, weight = FontWeight.ExtraBold),
        Font(Res.font.PublicSans_Black, weight = FontWeight.Black),
    )

@Composable
fun DisplayFontFamily() =
    FontFamily(
        Font(Res.font.Lexend_Thin, weight = FontWeight.Thin),
        Font(Res.font.Lexend_ExtraLight, weight = FontWeight.ExtraLight),
        Font(Res.font.Lexend_Light, weight = FontWeight.Light),
        Font(Res.font.Lexend_Regular, weight = FontWeight.Normal),
        Font(Res.font.Lexend_Medium, weight = FontWeight.Medium),
        Font(Res.font.Lexend_SemiBold, weight = FontWeight.SemiBold),
        Font(Res.font.Lexend_Bold, weight = FontWeight.Bold),
        Font(Res.font.Lexend_ExtraBold, weight = FontWeight.ExtraBold),
        Font(Res.font.Lexend_Black, weight = FontWeight.Black),
    )

@Composable
fun AppTypography() =
    Typography().run {
        val bodyFontFamily = BodyFontFamily()
        val disaplyFontFamily = DisplayFontFamily()

        Typography(
            displayLarge = displayLarge.copy(fontFamily = disaplyFontFamily),
            displayMedium = displayMedium.copy(fontFamily = disaplyFontFamily),
            displaySmall = displaySmall.copy(fontFamily = disaplyFontFamily),
            headlineLarge = headlineLarge.copy(fontFamily = disaplyFontFamily),
            headlineMedium = headlineMedium.copy(fontFamily = disaplyFontFamily),
            headlineSmall = headlineSmall.copy(fontFamily = disaplyFontFamily),
            titleLarge = titleLarge.copy(fontFamily = disaplyFontFamily),
            titleMedium = titleMedium.copy(fontFamily = disaplyFontFamily),
            titleSmall = titleSmall.copy(fontFamily = disaplyFontFamily),
            bodyLarge = bodyLarge.copy(fontFamily = bodyFontFamily),
            bodyMedium = bodyMedium.copy(fontFamily = bodyFontFamily),
            bodySmall = bodySmall.copy(fontFamily = bodyFontFamily),
            labelLarge = labelLarge.copy(fontFamily = bodyFontFamily),
            labelMedium = labelMedium.copy(fontFamily = bodyFontFamily),
            labelSmall = labelSmall.copy(fontFamily = bodyFontFamily),
        )
    }
