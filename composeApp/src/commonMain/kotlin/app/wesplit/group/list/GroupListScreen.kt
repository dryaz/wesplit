package app.wesplit.group.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.wesplit.account.GoogleLoginButton
import app.wesplit.domain.model.account.Account
import app.wesplit.domain.model.group.Group
import app.wesplit.ui.AdaptiveTopAppBar
import com.seiko.imageloader.rememberImagePainter
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveButton
import io.github.alexzhirkevich.cupertino.adaptive.ExperimentalAdaptiveApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.back_btn_cd
import split.composeapp.generated.resources.group_list_empty_description_authorized
import split.composeapp.generated.resources.group_list_empty_description_unregistered
import split.composeapp.generated.resources.group_list_title
import split.composeapp.generated.resources.ic_split_money
import split.composeapp.generated.resources.img_add_data
import split.composeapp.generated.resources.login
import split.composeapp.generated.resources.login_button_cd
import split.composeapp.generated.resources.or

sealed interface GroupListAction {
    data class Select(val group: Group) : GroupListAction

    data object CreateNewGroup : GroupListAction

    data object OpenMenu : GroupListAction

    data object Login : GroupListAction

    data object Logout : GroupListAction
}

@Composable
fun GroupListRoute(
    modifier: Modifier = Modifier,
    viewModel: GroupListViewModel,
    onAction: (GroupListAction) -> Unit,
) {
    val dataState = viewModel.dataState.collectAsState()
    val accountState = viewModel.accountState.collectAsState()

    GroupListScreen(
        modifier = modifier,
        dataState = dataState.value,
        accountState = accountState.value,
        onAction = onAction,
    )
}

// TODO: Check recomposition and probably postpone account retrivial?
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun GroupListScreen(
    modifier: Modifier = Modifier,
    dataState: GroupListViewModel.State,
    accountState: Account,
    onAction: (GroupListAction) -> Unit,
) {
    val windowSizeClass = calculateWindowSizeClass()

    val navigationIconClick =
        remember(windowSizeClass) {
            if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
                { onAction(GroupListAction.OpenMenu) }
            } else {
                null
            }
        }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            // TODO: Navigation drawer icon which also connected to menu items inside double pane nav
            AdaptiveTopAppBar(
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = stringResource(Res.string.back_btn_cd),
                    )
                },
                navigationTitle = { Unit },
                onNavigationIconClick = navigationIconClick,
                title = {
                    Text(stringResource(Res.string.group_list_title))
                },
                actions = {
                    Box(
                        modifier =
                            Modifier.size(48.dp).clickable {
                                onAction(GroupListAction.CreateNewGroup)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.AddCircle,
                            contentDescription = stringResource(Res.string.login_button_cd),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        },
    ) { paddings ->
        when (dataState) {
            GroupListViewModel.State.Empty -> {
                EmptyGroupList(modifier, accountState, onAction)
            }

            is GroupListViewModel.State.Groups ->
                GroupList(
                    modifier = Modifier.padding(paddings),
                    groups = dataState.groups,
                    onAction = { onAction(it) },
                    accountState = accountState,
                )
        }
    }
}

@Composable
private fun EmptyGroupList(
    modifier: Modifier = Modifier,
    accountState: Account,
    onAction: (GroupListAction) -> Unit,
) {
    AnimatedVisibility(
        modifier = modifier.fillMaxSize(1f),
        visible = accountState is Account.Unregistered,
    ) {
        EmptyGroupUnregistered(modifier, onAction)
    }

    AnimatedVisibility(
        modifier = modifier.fillMaxSize(1f),
        visible = accountState is Account.Authorized,
    ) {
        EmptyGroupAuthorized(modifier)
    }
}

@Composable
private fun EmptyGroupAuthorized(modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxSize(1f).padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            modifier = Modifier.alpha(0.7f),
            painter = painterResource(Res.drawable.img_add_data),
            contentDescription = stringResource(Res.string.back_btn_cd),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.group_list_empty_description_authorized),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun EmptyGroupUnregistered(
    modifier: Modifier,
    onAction: (GroupListAction) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize(1f),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(Res.string.group_list_empty_description_unregistered))
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(1f).padding(horizontal = 64.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(Res.string.or),
                style = MaterialTheme.typography.bodyMedium,
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        GoogleLoginButton { onAction(GroupListAction.Login) }
    }
}

@OptIn(ExperimentalAdaptiveApi::class)
@Composable
private fun GroupList(
    modifier: Modifier = Modifier,
    groups: List<Group>,
    accountState: Account,
    onAction: (GroupListAction) -> Unit,
) {
    val lazyColumnListState = rememberLazyListState()

    Column(modifier.fillMaxSize()) {
        // TODO: No yet scrollbars: https://developer.android.com/jetpack/androidx/compose-roadmap
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = lazyColumnListState,
        ) {
            items(items = groups, key = { it.id }) { group ->
                ListItem(
                    modifier =
                        Modifier.clickable {
                            onAction(GroupListAction.Select(group))
                        },
                    // TODO: Define View for group item
                    headlineContent = { Text("${group.title}") },
                    supportingContent = { Text("Users: ${group.users.size}") },
                    leadingContent = {
                        Box(contentAlignment = Alignment.Center) {
                            val painter =
                                rememberImagePainter(
                                    url = "https://randomuser.me/api/portraits/med/men/73.jpg",
                                    placeholderPainter = { painterResource(Res.drawable.ic_split_money) },
                                )
                            Image(
                                painter = painter,
                                contentScale = ContentScale.Fit,
                                contentDescription = group.title,
                                modifier = Modifier.size(42.dp).clip(CircleShape).aspectRatio(1f),
                            )
                        }
                    },
                )
                HorizontalDivider(modifier = Modifier.padding(start = 64.dp))
            }
        }
        AnimatedVisibility(visible = accountState is Account.Unregistered) {
            AdaptiveButton(
                modifier = Modifier.fillMaxWidth(1f),
                onClick = { onAction(GroupListAction.Login) },
            ) {
                Text(stringResource(Res.string.login))
            }
        }
    }
}
