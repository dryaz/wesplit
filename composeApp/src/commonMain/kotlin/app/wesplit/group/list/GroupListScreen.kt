package app.wesplit.group.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.wesplit.account.LoginSection
import app.wesplit.domain.model.account.Account
import app.wesplit.domain.model.account.Login
import app.wesplit.domain.model.group.Group
import app.wesplit.domain.model.group.uiTitle
import app.wesplit.domain.model.user.OnboardingStep
import app.wesplit.group.GroupImage
import app.wesplit.ui.AdaptiveTopAppBar
import app.wesplit.ui.Banner
import app.wesplit.ui.FeatureBanner
import app.wesplit.ui.tutorial.HelpOverlayPosition
import app.wesplit.ui.tutorial.LocalTutorialControl
import app.wesplit.ui.tutorial.TutorialItem
import app.wesplit.ui.tutorial.TutorialStep
import io.github.alexzhirkevich.cupertino.adaptive.icons.AdaptiveIcons
import io.github.alexzhirkevich.cupertino.adaptive.icons.AddCircle
import io.github.alexzhirkevich.cupertino.adaptive.icons.Menu
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.add_group_cd
import split.composeapp.generated.resources.back_btn_cd
import split.composeapp.generated.resources.group_list_empty_description_authorized
import split.composeapp.generated.resources.group_list_title
import split.composeapp.generated.resources.img_add_data
import split.composeapp.generated.resources.tutorial_step_create_group_description
import split.composeapp.generated.resources.tutorial_step_create_group_title
import split.composeapp.generated.resources.users_count

sealed interface GroupListAction {
    data class Select(val group: Group) : GroupListAction

    data object CreateNewGroup : GroupListAction

    data object OpenMenu : GroupListAction

    data class LoginWith(val login: Login) : GroupListAction

    data class BannerClick(val banner: Banner) : GroupListAction
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
    val tutorialControl = LocalTutorialControl.current
    val tutorialStep =
        remember {
            TutorialStep(
                title = Res.string.tutorial_step_create_group_title,
                description = Res.string.tutorial_step_create_group_description,
                onboardingStep = OnboardingStep.GROUP_ADD,
                helpOverlayPosition = HelpOverlayPosition.BOTTOM_LEFT,
                isModal = false,
            )
        }

    val navigationIconClick =
        remember(windowSizeClass) {
            if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
                { onAction(GroupListAction.OpenMenu) }
            } else {
                null
            }
        }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        topBar = {
            // TODO: Navigation drawer icon which also connected to menu items inside double pane nav
            AdaptiveTopAppBar(
                navigationIcon = {
                    Icon(
                        imageVector = AdaptiveIcons.Outlined.Menu,
                        contentDescription = stringResource(Res.string.back_btn_cd),
                    )
                },
                expandedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                navigationTitle = { Unit },
                onNavigationIconClick = navigationIconClick,
                title = {
                    Text(stringResource(Res.string.group_list_title))
                },
                actions = {
                    if (accountState is Account.Authorized) {
                        TutorialItem(
                            onPositioned = { rect -> tutorialControl.onPositionRecieved(tutorialStep, rect) },
                        ) { modifier ->
                            Icon(
                                modifier =
                                    modifier.minimumInteractiveComponentSize().clickable {
                                        tutorialControl.onNext()
                                        onAction(GroupListAction.CreateNewGroup)
                                    },
                                imageVector = AdaptiveIcons.Outlined.AddCircle,
                                contentDescription = stringResource(Res.string.add_group_cd),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
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
                    banner = dataState.banner,
                    onAction = { onAction(it) },
                )
        }
    }

    LaunchedEffect(accountState) {
        if (accountState is Account.Authorized) {
            tutorialControl.stepRequest(listOf(tutorialStep))
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
        visible = accountState is Account.Anonymous || accountState is Account.Restricted,
    ) {
        LoginSection(
            modifier = modifier,
            onLoginRequest = { login -> onAction(GroupListAction.LoginWith(login)) },
        )
    }

    AnimatedVisibility(
        modifier = modifier.fillMaxSize(1f),
        visible = accountState is Account.Authorized,
    ) {
        EmptyGroupAuthorized(modifier, onAction)
    }
}

@Composable
private fun EmptyGroupAuthorized(
    modifier: Modifier,
    onAction: (GroupListAction) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize(1f).padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            modifier =
                Modifier.clickable {
                    onAction(GroupListAction.CreateNewGroup)
                },
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
private fun GroupList(
    modifier: Modifier = Modifier,
    groups: List<Group>,
    banner: Banner?,
    onAction: (GroupListAction) -> Unit,
) {
    val lazyColumnListState = rememberLazyListState()

    Column(modifier.fillMaxSize()) {
        // TODO: No yet scrollbars: https://developer.android.com/jetpack/androidx/compose-roadmap
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = lazyColumnListState,
        ) {
            items(items = groups.take(2), key = { it.id }) { group ->
                GroupItem(onAction, group)
                HorizontalDivider(modifier = Modifier.padding(start = 64.dp))
            }

            banner?.let { adBanner ->
                item {
                    FeatureBanner(adBanner) {
                        onAction(GroupListAction.BannerClick(adBanner))
                    }
                }
            }

            if (groups.size > 2) {
                items(items = groups.takeLast(groups.size - 2), key = { it.id }) { group ->
                    GroupItem(onAction, group)
                    HorizontalDivider(modifier = Modifier.padding(start = 64.dp))
                }
            }
        }
    }
}

@Composable
private fun GroupItem(
    onAction: (GroupListAction) -> Unit,
    group: Group,
) {
    ListItem(
        colors =
            ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        modifier =
            Modifier.clickable {
                onAction(GroupListAction.Select(group))
            },
        // TODO: Define View for group item
        headlineContent = {
            Text(
                text = "${group.uiTitle()}",
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = { Text(stringResource(Res.string.users_count, group.participants.size)) },
        leadingContent = {
            GroupImage(
                imageUrl = group.imageUrl,
                groupTitle = group.uiTitle(),
                // TODO: Improvement: image generating - some magic anim
                isLoading = group.isImageGen,
            )
        },
    )
}
