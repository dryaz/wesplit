package app.wesplit.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.wesplit.theme.ColorMode
import app.wesplit.theme.LocalThemeState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.btn_google_dark
import split.composeapp.generated.resources.btn_google_light
import split.composeapp.generated.resources.login_with_google_cd

@Composable
fun GoogleLoginButton(onAction: () -> Unit) {
    Image(
        modifier = Modifier.clickable { onAction() },
        painter =
            painterResource(
                when (LocalThemeState.current.colorMode) {
                    ColorMode.LIGHT -> Res.drawable.btn_google_light
                    ColorMode.DARK -> Res.drawable.btn_google_dark
                    ColorMode.SYSTEM -> if (isSystemInDarkTheme()) Res.drawable.btn_google_dark else Res.drawable.btn_google_light
                },
            ),
        contentDescription = stringResource(Res.string.login_with_google_cd),
    )
}
