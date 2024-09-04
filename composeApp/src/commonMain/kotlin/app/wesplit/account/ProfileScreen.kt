package app.wesplit.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.account.Account
import app.wesplit.ui.AdaptiveTopAppBar
import com.seiko.imageloader.rememberImagePainter
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveButton
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveIconButton
import io.github.alexzhirkevich.cupertino.adaptive.ExperimentalAdaptiveApi
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.back_btn_cd
import split.composeapp.generated.resources.login
import split.composeapp.generated.resources.logout
import split.composeapp.generated.resources.profile

sealed interface ProfileAction {
    data object Login : ProfileAction

    data object Logout : ProfileAction

    data object OpenMenu : ProfileAction
}

@Composable
fun ProfileRoute(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel,
    onAction: (ProfileAction) -> Unit,
) {
    val accountState = viewModel.accountState.collectAsState()

    ProfileScreen(
        modifier = modifier,
        accountState = accountState.value,
        onAction = onAction,
    )
}

// TODO: Check recomposition and probably postpone account retrivial?
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalAdaptiveApi::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    accountState: Account,
    onAction: (ProfileAction) -> Unit,
) {
    val windowSizeClass = calculateWindowSizeClass()

    val navigationIconClick =
        remember(windowSizeClass) {
            if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
                { onAction(ProfileAction.OpenMenu) }
            } else {
                null
            }
        }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            AdaptiveTopAppBar(
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = stringResource(Res.string.back_btn_cd),
                    )
                },
                onNavigationIconClick = navigationIconClick,
                title = {
                    Text(stringResource(Res.string.profile))
                },
                actions = {
                    if (accountState is Account.Authorized) {
                        AdaptiveIconButton(onClick = { onAction(ProfileAction.Logout) }) {
                            Icon(
                                // TODO: Rework logout
                                Icons.AutoMirrored.Outlined.ExitToApp,
                                contentDescription = stringResource(Res.string.logout),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
            )
        },
    ) { paddings ->
        when (accountState) {
            is Account.Authorized ->
                AccountInfo(
                    modifier = Modifier.padding(paddings),
                    account = accountState,
                )

            Account.Unknown -> {
                Box(modifier = Modifier.fillMaxSize(1f)) {
                    CircularProgressIndicator()
                }
            }

            is Account.Anonymous,
            ->
                LoginSection(
                    modifier = modifier,
                    onAction = { loginAction ->
                        when (loginAction) {
                            LoginAction.Login -> onAction(ProfileAction.Login)
                        }
                    },
                )
        }
    }
}

@Composable
private fun Unregistered(
    modifier: Modifier = Modifier,
    onLogin: () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(1f),
        contentAlignment = Alignment.Center,
    ) {
        AdaptiveButton(
            modifier = Modifier.fillMaxWidth(1f),
            onClick = { onLogin() },
        ) {
            Text(stringResource(Res.string.login))
        }
    }
}

@Composable
private fun AccountInfo(
    modifier: Modifier = Modifier,
    account: Account.Authorized,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val painter = rememberImagePainter(account.user.photoURL ?: "")
        Image(
            modifier = Modifier.size(64.dp).clip(CircleShape),
            painter = painter,
            contentDescription = account.user.displayName,
        )
        Spacer(modifier = Modifier.height(8.dp))
        account.user.displayName?.let { name ->
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium,
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        account.user.email?.let { email ->
            Text(
                text = email,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}
