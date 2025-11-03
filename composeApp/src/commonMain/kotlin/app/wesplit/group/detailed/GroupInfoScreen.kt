package app.wesplit.group.detailed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest
import app.wesplit.ShareDelegate
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.currency.Amount
import app.wesplit.domain.model.expense.Expense
import app.wesplit.domain.model.expense.ExpenseRepository
import app.wesplit.domain.model.feature.FeatureAvailability
import app.wesplit.domain.model.group.Group
import app.wesplit.domain.model.group.GroupRepository
import app.wesplit.domain.model.group.Participant
import app.wesplit.domain.model.group.isMe
import app.wesplit.domain.model.group.uiTitle
import app.wesplit.domain.model.user.OnboardingStep
import app.wesplit.domain.model.user.UserRepository
import app.wesplit.group.ShareQrDialog
import app.wesplit.group.detailed.balance.BalanceList
import app.wesplit.group.detailed.expense.ExpenseAction
import app.wesplit.group.detailed.expense.ExpenseSection
import app.wesplit.group.detailed.expense.ExpenseSectionViewModel
import app.wesplit.ui.AdaptiveTopAppBar
import app.wesplit.ui.Banner
import app.wesplit.ui.molecules.GroupHead
import app.wesplit.ui.molecules.QuickAddAction
import app.wesplit.ui.molecules.QuickAddErrorState
import app.wesplit.ui.molecules.QuickAddState
import app.wesplit.ui.molecules.QuickAddValue
import app.wesplit.ui.molecules.isEmpty
import app.wesplit.ui.tutorial.HelpOverlayPosition
import app.wesplit.ui.tutorial.LocalTutorialControl
import app.wesplit.ui.tutorial.TutorialItem
import app.wesplit.ui.tutorial.TutorialStep
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.perf.performance
import io.github.alexzhirkevich.cupertino.adaptive.icons.AdaptiveIcons
import io.github.alexzhirkevich.cupertino.adaptive.icons.Add
import io.github.alexzhirkevich.cupertino.adaptive.icons.Done
import io.github.alexzhirkevich.cupertino.adaptive.icons.KeyboardArrowLeft
import io.github.alexzhirkevich.cupertino.adaptive.icons.Settings
import io.github.alexzhirkevich.cupertino.adaptive.icons.Share
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.add_expense_to_group
import split.composeapp.generated.resources.back
import split.composeapp.generated.resources.balances
import split.composeapp.generated.resources.csv_export_failed
import split.composeapp.generated.resources.csv_export_success
import split.composeapp.generated.resources.edit_group
import split.composeapp.generated.resources.error
import split.composeapp.generated.resources.export_csv
import split.composeapp.generated.resources.ic_file_csv
import split.composeapp.generated.resources.ic_user_add
import split.composeapp.generated.resources.join_group
import split.composeapp.generated.resources.loading
import split.composeapp.generated.resources.share_group
import split.composeapp.generated.resources.share_link_copied
import split.composeapp.generated.resources.transactions
import split.composeapp.generated.resources.tutorial_step_add_expense_description
import split.composeapp.generated.resources.tutorial_step_add_expense_title
import split.composeapp.generated.resources.tutorial_step_check_balances_description
import split.composeapp.generated.resources.tutorial_step_check_balances_title

private const val GROUP_OPEN_TRACE = "group_open"
private const val GROUP_OPEN_TRACE_THRESHOLD = 10_000L

private const val SHARE_QR_EVENT = "share_qr"

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

    data class ExportCsv(val group: Group) : GroupInfoAction
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
            title = Res.string.tutorial_step_check_balances_title,
            description = Res.string.tutorial_step_check_balances_description,
            onboardingStep = OnboardingStep.BALANCE_PREVIEW,
            isModal = true,
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
    val trace = remember { Firebase.performance.newTrace(GROUP_OPEN_TRACE) }
    val analyticsManager: AnalyticsManager = koinInject()

    LaunchedEffect(Unit) {
        trace.start()
    }

    LaunchedEffect(data.value) {
        if (data.value !is GroupInfoViewModel.State.Loading) {
            try {
                trace.stop()
            } catch (_: Throwable) {
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                trace.stop()
            } catch (_: Throwable) {
            }
        }
    }

    val coroutineScope = rememberCoroutineScope()

    val windowSizeClass = calculateWindowSizeClass()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val tutorialControl = LocalTutorialControl.current

    var quickAddInRowCounter by remember { mutableStateOf(0) }

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

    var quickAddData: QuickAddValue by remember { mutableStateOf(QuickAddValue()) }

    var quickAddError: QuickAddErrorState by remember { mutableStateOf(QuickAddErrorState.NONE) }

    var shareDialogVisibility by rememberSaveable { mutableStateOf(false) }
    var csvDialogVisibility by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(shareDialogVisibility) {
        if (shareDialogVisibility) analyticsManager.track(SHARE_QR_EVENT)
    }

    LaunchedEffect(Unit) {
        viewModel.event.collectLatest { event ->
            when (event) {
                is GroupInfoViewModel.Event.ShowCsvDialog -> {
                    csvDialogVisibility = true
                }

                is GroupInfoViewModel.Event.ShowPaywall -> {
                    onAction(GroupInfoAction.BannerClick(Banner.CSV_EXPORT))
                }

                is GroupInfoViewModel.Event.CsvExportSuccess -> {
                    // Show success message - share dialog was already shown by the delegate
                    scope.launch {
                        snackbarHostState.showSnackbar(getString(Res.string.csv_export_success))
                    }
                }

                is GroupInfoViewModel.Event.CsvExportError -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(getString(Res.string.csv_export_failed) + ": ${event.message}")
                    }
                }
            }
        }
    }

    val quickAddCommitCallback = {
        if (quickAddData.title.isNullOrBlank()) {
            quickAddError = QuickAddErrorState.TITLE
        } else if ((quickAddData.amount ?: 0.0) == 0.0) {
            quickAddError = QuickAddErrorState.AMOUNT
        } else {
            quickAddData.amount?.let { amount ->
                quickAddData.currencyCode?.let { currency ->
                    viewModel.quickAdd(
                        title = quickAddData.title,
                        amount =
                            Amount(
                                value = amount,
                                currencyCode = currency,
                            ),
                        inRow = quickAddInRowCounter++,
                    )
                    quickAddError = QuickAddErrorState.NONE
                    quickAddData = QuickAddValue()
                }
            }
        }
    }

    val actionCallback: (GroupInfoAction) -> Unit =
        remember {
            {
                when (it) {
                    is GroupInfoAction.Share -> shareDialogVisibility = true
                    is GroupInfoAction.ExportCsv -> viewModel.requestCsvExport()
                    else -> onAction(it)
                }
            }
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
                            if (!quickAddData.isEmpty()) {
                                quickAddCommitCallback()
                            } else {
                                tutorialControl.onNext()
                                onAction(GroupInfoAction.AddExpense(group))
                            }
                        },
                    ) {
                        Icon(
                            imageVector = if (!quickAddData.isEmpty()) AdaptiveIcons.Outlined.Done else AdaptiveIcons.Outlined.Add,
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
                                AdaptiveIcons.Outlined.Settings,
                                contentDescription = stringResource(Res.string.edit_group),
                            )
                        } else {
                            Icon(
                                painter = painterResource(Res.drawable.ic_user_add),
                                contentDescription = stringResource(Res.string.join_group),
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            (data.value as? GroupInfoViewModel.State.GroupInfo)?.group?.let { group ->
                                actionCallback.invoke(
                                    GroupInfoAction.ExportCsv(group),
                                )
                            }
                        },
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_file_csv),
                            contentDescription = stringResource(Res.string.export_csv),
                        )
                    }

                    IconButton(
                        modifier = modifier,
                        onClick = {
                            // TODO: Proper state handling, not just 1 groupinfo handler
                            tutorialControl.onNext()
                            (data.value as? GroupInfoViewModel.State.GroupInfo)?.group?.let { group ->
                                actionCallback.invoke(
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
                })
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize(1f).padding(top = padding.calculateTopPadding()),
            contentAlignment = Alignment.Center,
        ) {
            when (val state = data.value) {
                is GroupInfoViewModel.State.GroupInfo -> {
                    GroupInfoContent(
                        group = state.group,
                        onAction = actionCallback,
                        quickAddState =
                            when (state.quickAddFeature) {
                                FeatureAvailability.HIDE -> QuickAddState.Hidden
                                FeatureAvailability.PAYWAL -> QuickAddState.Paywall
                                FeatureAvailability.AVAIL ->
                                    QuickAddState.Data(
                                        value = quickAddData,
                                        error = quickAddError,
                                    )
                            },
                    ) { action ->
                        when (action) {
                            is QuickAddAction.Change -> quickAddData = action.value ?: QuickAddValue()
                            QuickAddAction.Commit -> quickAddCommitCallback()
                            QuickAddAction.RequestPaywall -> onAction(GroupInfoAction.BannerClick(Banner.QUICK_ADD))
                        }
                    }

                    AnimatedVisibility(visible = shareDialogVisibility) {
                        ShareQrDialog(
                            group = state.group,
                            isFullScreen = true,
                            onClose = {
                                shareDialogVisibility = false
                            },
                            onShare = {
                                shareDialogVisibility = false
                                onAction(GroupInfoAction.Share(state.group))
                            },
                        )
                    }

                    if (csvDialogVisibility) {
                        CsvExportDialog(
                            onDismiss = { csvDialogVisibility = false },
                            onExport = { includeShares ->
                                viewModel.exportCsv(includeShares)
                            },
                        )
                    }
                }

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
    quickAddState: QuickAddState,
    onQuickAddAction: (QuickAddAction) -> Unit,
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
            SplitView(
                expenseViewModel = expenseViewModel,
                group = group,
                quickAddState = quickAddState,
                onQuickAddAction = onQuickAddAction,
                onAction = actionInterceptor,
            )
        } else {
            PaginationView(
                expenseViewModel = expenseViewModel,
                group = group,
                quickAddState = quickAddState,
                onQuickAddValueChange = onQuickAddAction,
                onAction = actionInterceptor,
            )
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
    quickAddState: QuickAddState,
    onQuickAddAction: (QuickAddAction) -> Unit,
    onAction: (GroupInfoAction) -> Unit,
) {
    val tutorialControl = LocalTutorialControl.current

    Column {
        TabRow(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            selectedTabIndex = 2,
        ) {
            Tab(selected = false, onClick = {}, text = { Text(stringResource(Res.string.transactions)) })
            Tab(modifier = Modifier, selected = false, onClick = {
                tutorialControl.onNext()
            }, text = { Text(stringResource(Res.string.balances)) })
        }
    }
    Row {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.TopCenter,
        ) {
            ExpenseSection(
                viewModel = expenseViewModel,
                quickAddState = quickAddState,
                onQuickAddValueChange = onQuickAddAction,
            ) { action ->
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
    quickAddState: QuickAddState,
    onQuickAddValueChange: (QuickAddAction) -> Unit,
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
            Tab(modifier = Modifier, selected = selectedTabIndex == 1, onClick = {
                tutorialControl.onNext()
                selectedTabIndex = 1
            }, text = { Text(stringResource(Res.string.balances)) })
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
                        ExpenseSection(
                            viewModel = expenseViewModel,
                            quickAddState = quickAddState,
                            onQuickAddValueChange = onQuickAddValueChange,
                        ) { action ->
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
    Row(
        modifier = Modifier.padding(16.dp).fillMaxWidth(1f),
    ) {
        IconButton(
            modifier = Modifier,
            onClick = {
                onAction.invoke(GroupInfoAction.Back)
            },
        ) {
            Icon(
                AdaptiveIcons.Outlined.KeyboardArrowLeft,
                contentDescription = stringResource(Res.string.back),
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        GroupHead(
            modifier = Modifier.weight(1f),
            group = group,
        )
        IconButton(onClick = { onAction.invoke(GroupInfoAction.Edit(group)) }) {
            if (group.participants.any { it.isMe() }) {
                Icon(
                    AdaptiveIcons.Outlined.Settings,
                    contentDescription = stringResource(Res.string.edit_group),
                )
            } else {
                Icon(
                    painter = painterResource(Res.drawable.ic_user_add),
                    contentDescription = stringResource(Res.string.join_group),
                )
            }
        }

        IconButton(
            onClick = {
                onAction.invoke(GroupInfoAction.ExportCsv(group))
            },
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_file_csv),
                contentDescription = stringResource(Res.string.export_csv),
            )
        }

        IconButton(
            modifier = Modifier,
            onClick = {
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
