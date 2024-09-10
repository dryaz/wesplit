package app.wesplit.group.detailed

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.wesplit.domain.model.expense.ExpenseRepository
import app.wesplit.domain.model.group.Group
import app.wesplit.domain.model.group.balance.BalanceRepository
import app.wesplit.domain.model.group.uiTitle
import app.wesplit.group.detailed.balance.BalanceSection
import app.wesplit.group.detailed.balance.BalanceSectionViewModel
import app.wesplit.group.detailed.expense.ExpenseSection
import app.wesplit.group.detailed.expense.ExpenseSectionViewModel
import app.wesplit.participant.ParticipantAvatar
import app.wesplit.ui.AdaptiveTopAppBar
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.share_group

sealed interface GroupInfoAction {
    data object Back : GroupInfoAction
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun GroupInfoScreen(
    modifier: Modifier = Modifier,
    viewModel: GroupInfoViewModel,
    onAction: (GroupInfoAction) -> Unit,
) {
    val data = viewModel.dataState.collectAsState()

    val windowSizeClass = calculateWindowSizeClass()

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
                AdaptiveTopAppBar(
                    title = {},
                    onNavigationIconClick = { onAction(GroupInfoAction.Back) },
                )
            }
        },
    ) { paddings ->
        Box(
            modifier = Modifier.padding(paddings).fillMaxSize(1f),
            contentAlignment = Alignment.Center,
        ) {
            when (val state = data.value) {
                is GroupInfoViewModel.State.GroupInfo -> GroupInfoContent(state.group)
                GroupInfoViewModel.State.Loading ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator()
                    }

                is GroupInfoViewModel.State.Error -> Text("Error") // TODO: Error state
            }
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
private fun GroupInfoContent(group: Group) {
    val expenseRepository: ExpenseRepository = koinInject()
    val balanceRepository: BalanceRepository = koinInject()

    val expenseViewModel: ExpenseSectionViewModel =
        viewModel {
            ExpenseSectionViewModel(
                groupId = group.id,
                expenseRepository = expenseRepository,
            )
        }
    val balanceSectionViewModel: BalanceSectionViewModel =
        viewModel {
            BalanceSectionViewModel(
                groupId = group.id,
                balanceRepository = balanceRepository,
            )
        }

    Column(
        modifier = Modifier.fillMaxSize(1f),
    ) {
        GroupHeader(group)

        val windowSizeClass = calculateWindowSizeClass()
        if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
            SplitView(expenseViewModel, balanceSectionViewModel)
        } else {
            PaginationView(expenseViewModel, balanceSectionViewModel)
        }
    }
}

@Composable
private fun SplitView(
    expenseViewModel: ExpenseSectionViewModel,
    balanceSectionViewModel: BalanceSectionViewModel,
) {
    Column {
        TabRow(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            selectedTabIndex = 2,
        ) {
            Tab(
                selected = false,
                onClick = {},
                text = { Text("Transactions") },
            )
            Tab(
                selected = false,
                onClick = {},
                text = { Text("Balances") },
            )
        }
    }
    Row {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.TopCenter,
        ) {
            ExpenseSection(expenseViewModel)
        }
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.TopCenter,
        ) {
            BalanceSection(balanceSectionViewModel)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PaginationView(
    expenseViewModel: ExpenseSectionViewModel,
    balanceSectionViewModel: BalanceSectionViewModel,
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column {
        TabRow(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            selectedTabIndex = selectedTabIndex,
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("Transactions") },
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("Balances") },
            )
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
                    0 -> ExpenseSection(expenseViewModel)
                    1 -> BalanceSection(balanceSectionViewModel)
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
private fun GroupHeader(group: Group) {
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
        Icon(
            Icons.Filled.Share,
            contentDescription = stringResource(Res.string.share_group),
        )
    }
}
