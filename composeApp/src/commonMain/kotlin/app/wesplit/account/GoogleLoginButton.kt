package app.wesplit.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    var isLoading by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Image(
            modifier =
                Modifier.clickable {
                    isLoading = true
                    onAction()
                },
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
}
