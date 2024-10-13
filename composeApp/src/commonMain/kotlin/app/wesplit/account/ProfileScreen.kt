package app.wesplit.account

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
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
import app.wesplit.ShareData
import app.wesplit.ShareDelegate
import app.wesplit.domain.model.account.Account
import app.wesplit.domain.model.account.Login
import app.wesplit.domain.model.user.Subscription
import app.wesplit.domain.model.user.email
import app.wesplit.domain.model.user.participant
import app.wesplit.participant.ParticipantListItem
import app.wesplit.ui.AdaptiveTopAppBar
import app.wesplit.ui.PlusProtected
import io.github.alexzhirkevich.cupertino.adaptive.icons.AdaptiveIcons
import io.github.alexzhirkevich.cupertino.adaptive.icons.Delete
import io.github.alexzhirkevich.cupertino.adaptive.icons.Done
import io.github.alexzhirkevich.cupertino.adaptive.icons.KeyboardArrowRight
import io.github.alexzhirkevich.cupertino.adaptive.icons.Lock
import io.github.alexzhirkevich.cupertino.adaptive.icons.Menu
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.back_btn_cd
import split.composeapp.generated.resources.plus_feature_currencies_descr_short
import split.composeapp.generated.resources.plus_feature_currencies_title
import split.composeapp.generated.resources.plus_feature_more_descr
import split.composeapp.generated.resources.plus_feature_more_title
import split.composeapp.generated.resources.plus_feature_protect_descr_short
import split.composeapp.generated.resources.plus_feature_protect_title
import split.composeapp.generated.resources.profile

sealed interface ProfileAction {
    data class LoginWith(val login: Login) : ProfileAction

    data object Logout : ProfileAction

    data object OpenMenu : ProfileAction

    data object Paywall : ProfileAction
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
    ) { padding ->
        when (accountState) {
            is Account.Authorized ->
                AccountInfo(
                    modifier = Modifier.padding(padding),
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

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
private fun AccountInfo(
    modifier: Modifier = Modifier,
    account: Account.Authorized,
    onAction: (ProfileAction) -> Unit,
    onAccountDelete: () -> Unit,
) {
    var deleteDialogShown by remember { mutableStateOf(false) }
    val participant = account.user.participant()
    val windowSizeClass = calculateWindowSizeClass()
    val shareDelegate: ShareDelegate = koinInject()

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        participant?.let {
            ParticipantListItem(
                modifier = Modifier.fillMaxWidth(1f).padding(horizontal = 8.dp),
                participant = it,
                showImage = windowSizeClass.heightSizeClass != WindowHeightSizeClass.Compact,
                showMeBadge = false,
                subComposable =
                    account.user.email()?.let { email ->
                        {
                            Text(
                                text = email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline,
                            )
                        }
                    },
            )
        }

        Plan(account) { onAction(ProfileAction.Paywall) }
        Spacer(modifier = Modifier.weight(1f))

        val uriHandler = LocalUriHandler.current
        ListItem(
            modifier =
                Modifier.clickable {
                    if (shareDelegate.supportPlatformSharing()) {
                        shareDelegate.open(ShareData.Link("https://wesplit.app/privacypolicy/"))
                    } else {
                        uriHandler.openUri("https://wesplit.app/privacypolicy/")
                    }
                },
            colors =
                ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
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
                    if (shareDelegate.supportPlatformSharing()) {
                        shareDelegate.open(ShareData.Link("https://wesplit.app/terms/"))
                    } else {
                        uriHandler.openUri("https://wesplit.app/terms/")
                    }
                },
            colors =
                ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
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
private fun ColumnScope.Plan(
    account: Account.Authorized,
    onSubscribe: () -> Unit,
) {
    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            ),
        modifier = Modifier.fillMaxWidth(1f).padding(16.dp),
    ) {
        val title =
            when (account.user.subscription) {
                Subscription.BASIC -> "Try Plus"
                Subscription.PLUS -> "Your have Plus+"
            }

        val desc =
            when (account.user.subscription) {
                Subscription.BASIC -> "Unlock all features"
                Subscription.PLUS -> "All features unlocked"
            }

        val modifier =
            when (account.user.subscription) {
                Subscription.BASIC -> Modifier.fillMaxWidth().clickable { onSubscribe() }
                Subscription.PLUS -> Modifier
            }

        val trailing: @Composable (() -> Unit)? =
            when (account.user.subscription) {
                Subscription.BASIC -> {
                    {
                        Icon(
                            modifier = Modifier.minimumInteractiveComponentSize(),
                            imageVector = AdaptiveIcons.Outlined.KeyboardArrowRight,
                            contentDescription = "Subscribe to Plus",
                        )
                    }
                }

                Subscription.PLUS -> null
            }

        ListItem(
            modifier = modifier,
            colors =
                ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
            headlineContent = {
                PlusProtected {
                    Text(
                        text = title,
                    )
                }
            },
            supportingContent = {
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
            },
            trailingContent = trailing,
        )

        HorizontalDivider()

        FeaturesList(
            subscription = account.user.subscription,
            onSubscribe = onSubscribe,
        )

        AnimatedVisibility(
            modifier = Modifier.fillMaxWidth(1f),
            visible = account.user.subscription == Subscription.BASIC,
        ) {
            ListItem(
                modifier =
                    Modifier.fillMaxWidth().clickable {
                        onSubscribe()
                    },
                colors =
                    ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        headlineColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                headlineContent = {
                    Text(
                        modifier = Modifier.minimumInteractiveComponentSize().fillMaxWidth(1f),
                        text = "Subscribe to Plus",
                        textAlign = TextAlign.Center,
                    )
                },
            )
        }
    }
}

@Composable
private fun FeaturesList(
    subscription: Subscription,
    onSubscribe: () -> Unit,
) {
    val icon =
        when (subscription) {
            Subscription.BASIC -> AdaptiveIcons.Outlined.Lock
            Subscription.PLUS -> AdaptiveIcons.Outlined.Done
        }

    val featuresMap =
        mapOf(
            Res.string.plus_feature_protect_title to Res.string.plus_feature_protect_descr_short,
            Res.string.plus_feature_currencies_title to Res.string.plus_feature_currencies_descr_short,
            Res.string.plus_feature_more_title to Res.string.plus_feature_more_descr,
        )
    featuresMap.map { feature ->
        ListItem(
            modifier =
                Modifier.fillMaxWidth().clickable {
                    onSubscribe()
                },
            colors =
                ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
            leadingContent = {
                Icon(
                    modifier = Modifier.minimumInteractiveComponentSize(),
                    imageVector = icon,
                    contentDescription = stringResource(feature.value),
                )
            },
            headlineContent = {
                Text(
                    text = stringResource(feature.key),
                )
            },
            supportingContent = {
                Text(
                    text = stringResource(feature.value),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
            },
        )
    }
}
