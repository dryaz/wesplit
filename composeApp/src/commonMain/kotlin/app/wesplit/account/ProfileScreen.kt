package app.wesplit.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.account.Account
import app.wesplit.domain.model.user.email
import app.wesplit.ui.AdaptiveTopAppBar
import com.seiko.imageloader.rememberImagePainter
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveButton
import io.github.alexzhirkevich.cupertino.adaptive.ExperimentalAdaptiveApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.back_btn_cd
import split.composeapp.generated.resources.img_construct
import split.composeapp.generated.resources.login
import split.composeapp.generated.resources.profile
import split.composeapp.generated.resources.profile_under_construction

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
            )
        },
    ) { paddings ->
        when (accountState) {
            is Account.Authorized ->
                AccountInfo(
                    modifier = Modifier.padding(paddings),
                    account = accountState,
                    onAction = onAction,
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

@OptIn(ExperimentalAdaptiveApi::class)
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
    onAction: (ProfileAction) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        val painter = rememberImagePainter(account.user.photoUrl ?: "")
        Image(
            modifier = Modifier.size(64.dp).clip(CircleShape),
            painter = painter,
            contentDescription = account.user.name,
        )
        Spacer(modifier = Modifier.height(8.dp))
        account.user.name?.let { name ->
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium,
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        account.user.email()?.let { email ->
            Text(
                text = email,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
        UnderConstruction()
        val uriHandler = LocalUriHandler.current
        ListItem(
            modifier =
                Modifier.clickable {
                    uriHandler.openUri("https://wesplit.app/privacypolicy/")
                },
            colors =
                ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            trailingContent = {
                Icon(
                    Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = "Go to privacy policy",
                    tint = MaterialTheme.colorScheme.outline,
                )
            },
            headlineContent = {
                Text(
                    text = "Privacy policy",
                    color = MaterialTheme.colorScheme.outline,
                )
            },
        )

        ListItem(
            modifier =
                Modifier.clickable {
                    uriHandler.openUri("https://wesplit.app/terms/")
                },
            colors =
                ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            trailingContent = {
                Icon(
                    Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = "Go to terms and conditions",
                    tint = MaterialTheme.colorScheme.outline,
                )
            },
            headlineContent = {
                Text(
                    text = "Terms&conditions",
                    color = MaterialTheme.colorScheme.outline,
                )
            },
        )

        ListItem(
            modifier = Modifier.clickable { onAction(ProfileAction.Logout) },
            colors =
                ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    headlineColor = MaterialTheme.colorScheme.error,
                ),
            headlineContent = {
                Text(
                    modifier = Modifier.fillMaxWidth(1f),
                    textAlign = TextAlign.Center,
                    text = "Logout",
                )
            },
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ColumnScope.UnderConstruction() {
    Column(
        modifier = Modifier.weight(1f).padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            modifier = Modifier,
            painter = painterResource(Res.drawable.img_construct),
            contentDescription = stringResource(Res.string.profile_under_construction),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.profile_under_construction),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}
