package app.wesplit

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import app.wesplit.domain.model.user.UserRepository
import app.wesplit.domain.model.user.isPlus
import app.wesplit.theme.ColorMode
import app.wesplit.theme.LocalThemeState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.app_name
import split.composeapp.generated.resources.img_logo_black
import split.composeapp.generated.resources.img_logo_color
import split.composeapp.generated.resources.img_logo_color_plus
import split.composeapp.generated.resources.img_logo_white

@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    isColorfull: Boolean = true,
) {
    val userRepository: UserRepository = koinInject()
    val user = userRepository.get().collectAsState()

    val isPlus by remember(user) {
        derivedStateOf {
            user.value?.isPlus() == true
        }
    }

    Image(
        modifier = modifier,
        painter =
            painterResource(
                if (isColorfull) {
                    if (isPlus) Res.drawable.img_logo_color_plus else Res.drawable.img_logo_color
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
