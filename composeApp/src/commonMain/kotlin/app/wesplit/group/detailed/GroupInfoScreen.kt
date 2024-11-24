package app.wesplit.group.detailed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.wesplit.ShareDelegate
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.expense.Expense
import app.wesplit.domain.model.expense.ExpenseRepository
import app.wesplit.domain.model.group.Group
import app.wesplit.domain.model.group.GroupRepository
import app.wesplit.domain.model.group.Participant
import app.wesplit.domain.model.group.isMe
import app.wesplit.domain.model.group.uiTitle
import app.wesplit.domain.model.user.OnboardingStep
import app.wesplit.domain.model.user.UserRepository
import app.wesplit.group.detailed.balance.BalanceList
import app.wesplit.group.detailed.expense.ExpenseAction
import app.wesplit.group.detailed.expense.ExpenseSection
import app.wesplit.group.detailed.expense.ExpenseSectionViewModel
import app.wesplit.participant.ParticipantAvatar
import app.wesplit.ui.AdaptiveTopAppBar
import app.wesplit.ui.Banner
import app.wesplit.ui.tutorial.HelpOverlayPosition
import app.wesplit.ui.tutorial.LocalTutorialControl
import app.wesplit.ui.tutorial.TutorialItem
import app.wesplit.ui.tutorial.TutorialStep
import io.github.alexzhirkevich.cupertino.adaptive.icons.AdaptiveIcons
import io.github.alexzhirkevich.cupertino.adaptive.icons.Add
import io.github.alexzhirkevich.cupertino.adaptive.icons.Edit
import io.github.alexzhirkevich.cupertino.adaptive.icons.Share
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.add_expense_to_group
import split.composeapp.generated.resources.balances
import split.composeapp.generated.resources.edit_group
import split.composeapp.generated.resources.error
import split.composeapp.generated.resources.ic_user_add
import split.composeapp.generated.resources.join_group
import split.composeapp.generated.resources.loading
import split.composeapp.generated.resources.share_group
import split.composeapp.generated.resources.share_link_copied
import split.composeapp.generated.resources.transactions
import split.composeapp.generated.resources.tutorial_step_add_expense_description
import split.composeapp.generated.resources.tutorial_step_add_expense_title
import split.composeapp.generated.resources.tutorial_step_balances_tab_description
import split.composeapp.generated.resources.tutorial_step_balances_tab_title
import split.composeapp.generated.resources.tutorial_step_check_balances_description
import split.composeapp.generated.resources.tutorial_step_check_balances_title
import split.composeapp.generated.resources.tutorial_step_invite_friends_description
import split.composeapp.generated.resources.tutorial_step_invite_friends_title
import split.composeapp.generated.resources.tutorial_step_settle_up_description
import split.composeapp.generated.resources.tutorial_step_settle_up_title

sealed interface GroupInfoAction {
    data object Back : GroupInfoAction

    data class AddExpense(val group: Group) : GroupInfoAction

    // TODO: How to share group
    data class Share(val group: Group) : GroupInfoAction

    data class Invite(val participant: Participant) : GroupInfoAction

    data class Edit(val group: Group) : GroupInfoAction

    data class OpenExpenseDetails(val expense: Expense) : GroupInfoAction

    data class Settle(val group: Group) : GroupInfoAction

    data class BannerClick(val banner: Banner) : GroupInfoAction
}

private val addExpenseTutorialStep =
    TutorialStep(
        title = Res.string.tutorial_step_add_expense_title,
        description = Res.string.tutorial_step_add_expense_description,
        onboardingStep = OnboardingStep.EXPENSE_ADD,
        isModal = false,
        helpOverlayPosition = HelpOverlayPosition.TOP_LEFT,
    )

internal val checkBalanceTutorialStepFlow =
    listOf(
        TutorialStep(
            title = Res.string.tutorial_step_balances_tab_title,
            description = Res.string.tutorial_step_balances_tab_description,
            onboardingStep = OnboardingStep.BALANCE_CHOOSER,
            isModal = false,
            helpOverlayPosition = HelpOverlayPosition.BOTTOM_LEFT,
        ),
        TutorialStep(
            title = Res.string.tutorial_step_check_balances_title,
            description = Res.string.tutorial_step_check_balances_description,
            onboardingStep = OnboardingStep.BALANCE_PREVIEW,
            isModal = true,
            helpOverlayPosition = HelpOverlayPosition.BOTTOM_LEFT,
        ),
        TutorialStep(
            title = Res.string.tutorial_step_settle_up_title,
            description = Res.string.tutorial_step_settle_up_description,
            onboardingStep = OnboardingStep.SETTLE_UP,
            isModal = true,
            helpOverlayPosition = HelpOverlayPosition.BOTTOM_LEFT,
        ),
        TutorialStep(
            title = Res.string.tutorial_step_invite_friends_title,
            description = Res.string.tutorial_step_invite_friends_description,
            onboardingStep = OnboardingStep.SHARE_GROUP,
            isModal = false,
            helpOverlayPosition = HelpOverlayPosition.BOTTOM_LEFT,
        ),
    )

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun GroupInfoScreen(
    modifier: Modifier = Modifier,
    viewModel: GroupInfoViewModel,
    shareDelegate: ShareDelegate,
    onAction: (GroupInfoAction) -> Unit,
) {
    val data = viewModel.dataState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val windowSizeClass = calculateWindowSizeClass()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val tutorialControl = LocalTutorialControl.current

    val isMeParticipating =
        remember(data.value) {
            (data.value as? GroupInfoViewModel.State.GroupInfo)?.group?.participants?.any { it.isMe() } ?: false
        }

    fun showLinkCopiedSnackbar() {
        scope.launch {
            snackbarHostState.showSnackbar(getString(Res.string.share_link_copied))
        }
    }

    LaunchedEffect(Unit) {
        tutorialControl.stepRequest(listOf(addExpenseTutorialStep))
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        floatingActionButton = {
            (data.value as? GroupInfoViewModel.State.GroupInfo)?.group?.let { group ->
                TutorialItem(
                    onPositioned = { tutorialControl.onPositionRecieved(addExpenseTutorialStep, it) },
                ) { modifier ->
                    FloatingActionButton(
                        modifier = modifier,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        onClick = {
                            tutorialControl.onNext()
                            onAction(GroupInfoAction.AddExpense(group))
                        },
                    ) {
                        Icon(
                            AdaptiveIcons.Outlined.Add,
                            contentDescription = stringResource(Res.string.add_expense_to_group),
                        )
                    }
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact ||
                windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact
            ) {
                // TODO: Better to have collapsing toolbar instead of hiding group header
                AdaptiveTopAppBar(title = {
                    Text(
                        text =
                            when (val state = data.value) {
                                is GroupInfoViewModel.State.Error -> stringResource(Res.string.error)
                                is GroupInfoViewModel.State.GroupInfo -> state.group.uiTitle()
                                GroupInfoViewModel.State.Loading -> stringResource(Res.string.loading)
                            },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }, onNavigationIconClick = { onAction(GroupInfoAction.Back) }, actions = {
                    IconButton(onClick = {
                        // TODO: Proper state handling, not just 1 groupinfo handler
                        coroutineScope.launch(NonCancellable) {
                            (data.value as? GroupInfoViewModel.State.GroupInfo)?.group?.let { group ->
                                onAction.invoke(
                                    GroupInfoAction.Edit(group),
                                )
                            }
                        }
                    }) {
                        if (isMeParticipating) {
                            Icon(
                                AdaptiveIcons.Outlined.Edit,
                                contentDescription = stringResource(Res.string.edit_group),
                            )
                        } else {
                            Icon(
                                painter = painterResource(Res.drawable.ic_user_add),
                                contentDescription = stringResource(Res.string.join_group),
                            )
                        }
                    }

                    TutorialItem(
                        onPositioned = { tutorialControl.onPositionRecieved(checkBalanceTutorialStepFlow[3], it) },
                    ) { modifier ->
                        IconButton(
                            modifier = modifier,
                            onClick = {
                                // TODO: Proper state handling, not just 1 groupinfo handler
                                tutorialControl.onNext()
                                (data.value as? GroupInfoViewModel.State.GroupInfo)?.group?.let { group ->
                                    onAction.invoke(
                                        GroupInfoAction.Share(group),
                                    )
                                }
                            },
                        ) {
                            Icon(
                                AdaptiveIcons.Outlined.Share,
                                contentDescription = stringResource(Res.string.share_group),
                            )
                        }
                    }
                })
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize(1f).padding(top = padding.calculateTopPadding()),
            contentAlignment = Alignment.Center,
        ) {
            val actionCallback: (GroupInfoAction) -> Unit =
                remember {
                    {
                        if (it is GroupInfoAction.Share && !shareDelegate.supportPlatformSharing()) showLinkCopiedSnackbar()
                        onAction(it)
                    }
                }

            when (val state = data.value) {
                is GroupInfoViewModel.State.GroupInfo -> GroupInfoContent(state.group, actionCallback)
                GroupInfoViewModel.State.Loading ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator()
                    }

                is GroupInfoViewModel.State.Error -> Text(stringResource(Res.string.error)) // TODO: Error state
            }
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
private fun GroupInfoContent(
    group: Group,
    onAction: (GroupInfoAction) -> Unit,
) {
    val expenseRepository: ExpenseRepository = koinInject()
    val groupRepository: GroupRepository = koinInject()
    val analyticsManager: AnalyticsManager = koinInject()
    val userRepository: UserRepository = koinInject()
    val tutorialControl = LocalTutorialControl.current

    val windowSizeClass = calculateWindowSizeClass()

    val expenseViewModel: ExpenseSectionViewModel =
        viewModel(key = "ExpenseSectionViewModel ${group.id}") {
            ExpenseSectionViewModel(
                groupId = group.id,
                expenseRepository = expenseRepository,
                groupRepository = groupRepository,
                analyticsManager = analyticsManager,
                userRepository = userRepository,
            )
        }

    // TODO: Hack to share group instead of invite participant (in future).
    val actionInterceptor: (GroupInfoAction) -> Unit =
        remember(group, onAction) {
            { action ->
                if (action is GroupInfoAction.Invite) {
                    onAction(GroupInfoAction.Share(group))
                } else {
                    onAction(action)
                }
            }
        }

    Column(
        modifier = Modifier.fillMaxSize(1f),
    ) {
        if (windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact &&
            windowSizeClass.heightSizeClass != WindowHeightSizeClass.Compact
        ) {
            GroupHeader(group, actionInterceptor)
        }

        if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded &&
            windowSizeClass.heightSizeClass != WindowHeightSizeClass.Compact
        ) {
            SplitView(expenseViewModel, group, actionInterceptor)
        } else {
            PaginationView(expenseViewModel, group, actionInterceptor)
        }
    }

    LaunchedEffect(group.balances) {
        if (group.balances?.participantsBalance?.any { it.amounts.any { it.value != 0.0 } } == true) {
            tutorialControl.stepRequest(checkBalanceTutorialStepFlow)
        }
    }
}

@Composable
private fun SplitView(
    expenseViewModel: ExpenseSectionViewModel,
    group: Group,
    onAction: (GroupInfoAction) -> Unit,
) {
    val tutorialControl = LocalTutorialControl.current

    Column {
        TabRow(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            selectedTabIndex = 2,
        ) {
            Tab(selected = false, onClick = {}, text = { Text(stringResource(Res.string.transactions)) })
            TutorialItem(
                onPositioned = { tutorialControl.onPositionRecieved(checkBalanceTutorialStepFlow[0], it) },
            ) { modifier ->
                Tab(modifier = modifier, selected = false, onClick = {
                    tutorialControl.onNext()
                }, text = { Text(stringResource(Res.string.balances)) })
            }
        }
    }
    Row {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.TopCenter,
        ) {
            ExpenseSection(expenseViewModel) { action ->
                when (action) {
                    is ExpenseAction.OpenDetails -> onAction(GroupInfoAction.OpenExpenseDetails(action.expense))
                    is ExpenseAction.BannerClick -> onAction(GroupInfoAction.BannerClick(action.banner))
                }
            }
        }
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.TopCenter,
        ) {
            BalanceList(
                balance = group.balances,
                onInvite = { onAction(GroupInfoAction.Invite(it)) },
            ) {
                onAction(GroupInfoAction.Settle(group))
            }
        }
    }
}

@Composable
private fun PaginationView(
    expenseViewModel: ExpenseSectionViewModel,
    group: Group,
    onAction: (GroupInfoAction) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tutorialControl = LocalTutorialControl.current

    Column {
        TabRow(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            selectedTabIndex = selectedTabIndex,
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text(stringResource(Res.string.transactions)) },
            )
            TutorialItem(
                onPositioned = { tutorialControl.onPositionRecieved(checkBalanceTutorialStepFlow[0], it) },
            ) { modifier ->
                Tab(modifier = modifier, selected = selectedTabIndex == 1, onClick = {
                    tutorialControl.onNext()
                    selectedTabIndex = 1
                }, text = { Text(stringResource(Res.string.balances)) })
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(1f),
        ) { index ->
            Box(
                modifier = Modifier.fillMaxSize(1f),
                contentAlignment = Alignment.TopCenter,
            ) {
                when (index) {
                    0 ->
                        ExpenseSection(expenseViewModel) { action ->
                            when (action) {
                                is ExpenseAction.OpenDetails -> onAction(GroupInfoAction.OpenExpenseDetails(action.expense))
                                is ExpenseAction.BannerClick -> onAction(GroupInfoAction.BannerClick(action.banner))
                            }
                        }

                    1 ->
                        BalanceList(
                            balance = group.balances,
                            onInvite = { onAction(GroupInfoAction.Invite(it)) },
                        ) {
                            onAction(GroupInfoAction.Settle(group))
                        }
                }
            }
        }
    }

    LaunchedEffect(key1 = selectedTabIndex) {
        pagerState.animateScrollToPage(selectedTabIndex)
    }

    LaunchedEffect(key1 = pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            selectedTabIndex = pagerState.currentPage
        }
    }
}

@Composable
private fun GroupHeader(
    group: Group,
    onAction: (GroupInfoAction) -> Unit,
) {
    val tutorialControl = LocalTutorialControl.current
    Row(
        modifier = Modifier.padding(16.dp).fillMaxWidth(1f),
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = group.uiTitle(),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box {
                group.participants.forEachIndexed { index, participant ->
                    ParticipantAvatar(
                        modifier = Modifier.padding(start = 20.dp * index),
                        participant = participant,
                        size = 36.dp,
                    )
                }
            }
        }
        IconButton(onClick = { onAction.invoke(GroupInfoAction.Edit(group)) }) {
            if (group.participants.any { it.isMe() }) {
                Icon(
                    AdaptiveIcons.Outlined.Edit,
                    contentDescription = stringResource(Res.string.edit_group),
                )
            } else {
                Icon(
                    painter = painterResource(Res.drawable.ic_user_add),
                    contentDescription = stringResource(Res.string.join_group),
                )
            }
        }

        TutorialItem(
            onPositioned = { tutorialControl.onPositionRecieved(checkBalanceTutorialStepFlow[3], it) },
        ) { modifier ->
            IconButton(
                modifier = modifier,
                onClick = {
                    tutorialControl.onNext()
                    onAction.invoke(GroupInfoAction.Share(group))
                },
            ) {
                Icon(
                    AdaptiveIcons.Outlined.Share,
                    contentDescription = stringResource(Res.string.share_group),
                )
            }
        }
    }
}
