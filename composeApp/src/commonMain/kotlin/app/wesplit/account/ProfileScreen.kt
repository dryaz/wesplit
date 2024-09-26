package app.wesplit.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.account.Account
import app.wesplit.domain.model.account.Login
import app.wesplit.domain.model.account.participant
import app.wesplit.domain.model.user.email
import app.wesplit.participant.ParticipantAvatar
import app.wesplit.ui.AdaptiveTopAppBar
import io.github.alexzhirkevich.cupertino.adaptive.icons.AdaptiveIcons
import io.github.alexzhirkevich.cupertino.adaptive.icons.Delete
import io.github.alexzhirkevich.cupertino.adaptive.icons.KeyboardArrowRight
import io.github.alexzhirkevich.cupertino.adaptive.icons.Menu
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.back_btn_cd
import split.composeapp.generated.resources.img_construct
import split.composeapp.generated.resources.profile
import split.composeapp.generated.resources.profile_under_construction

sealed interface ProfileAction {
    data class LoginWith(val login: Login) : ProfileAction

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
        onAccountDelete = {
            viewModel.deleteAccount()
        },
    )
}

// TODO: Check recomposition and probably postpone account retrivial?
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    accountState: Account,
    onAction: (ProfileAction) -> Unit,
    onAccountDelete: () -> Unit,
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
                        imageVector = AdaptiveIcons.Outlined.Menu,
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
                    onAccountDelete = onAccountDelete,
                )

            Account.Unknown -> {
                Box(modifier = Modifier.fillMaxSize(1f)) {
                    CircularProgressIndicator()
                }
            }

            Account.Restricted,
            is Account.Anonymous,
            ->
                LoginSection(
                    modifier = modifier,
                    onLoginRequest = { login -> onAction(ProfileAction.LoginWith(login)) },
                )
        }
    }
}

@Composable
private fun AccountInfo(
    modifier: Modifier = Modifier,
    account: Account.Authorized,
    onAction: (ProfileAction) -> Unit,
    onAccountDelete: () -> Unit,
) {
    var deleteDialogShown by remember { mutableStateOf(false) }
    val participant = account.participant()

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        participant?.let {
            ParticipantAvatar(participant = it)
        }
        Spacer(modifier = Modifier.height(8.dp))
        account.user.name.let { name ->
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
                    AdaptiveIcons.Outlined.KeyboardArrowRight,
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
                    AdaptiveIcons.Outlined.KeyboardArrowRight,
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

        Row {
            ListItem(
                modifier = Modifier.weight(1f).clickable { deleteDialogShown = true },
                colors =
                    ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        headlineColor = MaterialTheme.colorScheme.error,
                    ),
                headlineContent = {
                    Text(
                        modifier = Modifier.fillMaxWidth(1f),
                        textAlign = TextAlign.Center,
                        text = "Delete account",
                    )
                },
            )

            ListItem(
                modifier = Modifier.weight(1f).clickable { onAction(ProfileAction.Logout) },
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
        }

        Spacer(modifier = Modifier.height(8.dp))
    }

    if (deleteDialogShown) {
        AlertDialog(
            modifier = Modifier.widthIn(max = 450.dp),
            onDismissRequest = { deleteDialogShown = false },
            title = { Text("Delete Account?") },
            text = {
                Text(
                    text = "Are you sure that \nyou want completely delete your account?",
                    textAlign = TextAlign.Center,
                )
            },
            icon = {
                Icon(
                    AdaptiveIcons.Outlined.Delete,
                    contentDescription = "Delete account from wesplit",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { onAccountDelete() },
                ) {
                    Text(
                        text = "Yes, Delete",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        deleteDialogShown = false
                    },
                ) {
                    Text("No, Wait")
                }
            },
        )
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
