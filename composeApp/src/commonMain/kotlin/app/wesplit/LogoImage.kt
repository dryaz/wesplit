package app.wesplit

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.wesplit.theme.ColorMode
import app.wesplit.theme.LocalThemeState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.app_name
import split.composeapp.generated.resources.img_logo_black
import split.composeapp.generated.resources.img_logo_color
import split.composeapp.generated.resources.img_logo_white

@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    isColorfull: Boolean = true,
) {
    Image(
        modifier = modifier,
        painter =
            painterResource(
                if (isColorfull) {
                    Res.drawable.img_logo_color
                } else {
                    when (LocalThemeState.current.colorMode) {
                        ColorMode.LIGHT -> Res.drawable.img_logo_black
                        ColorMode.DARK -> Res.drawable.img_logo_white
                        ColorMode.SYSTEM -> if (isSystemInDarkTheme()) Res.drawable.img_logo_white else Res.drawable.img_logo_black
                    }
                },
            ),
        contentDescription = stringResource(Res.string.app_name),
    )
}
