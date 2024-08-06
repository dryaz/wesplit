package group.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.account.Account
import app.wesplit.domain.model.group.Group
import org.koin.compose.viewmodel.koinViewModel

sealed class GroupItemAction {
    abstract val group: Group

    data class Select(override val group: Group) : GroupItemAction()
}

@Composable
fun GroupListScreen(modifier: Modifier = Modifier, viewModel: GroupListViewModel = koinViewModel(), onAction: (GroupItemAction) -> Unit) {
    println("c1")
    val dataState = viewModel.dataState.collectAsState()
    println("c2")
    val accountState = viewModel.accountState.collectAsState()
    println("c3")

    Screen(
        modifier = modifier,
        dataState = dataState.value,
        accountState = accountState.value,
        onAction = onAction
    )
}

// TODO: Check recomposition and probably postpone account retrivial?
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Screen(
    modifier: Modifier = Modifier,
    dataState: GroupListViewModel.State,
    accountState: Account,
    onAction: (GroupItemAction) -> Unit
) {
    println("d1")
    Scaffold(modifier = modifier, topBar = {
        TopAppBar(title = {
            Text(
                when (val state = accountState) {
                    is Account.Authorized -> state.name
                    Account.Unknown -> "Loading"
                    Account.Unregistered -> "Unauthorized"
                }
            )
        })
    }) { paddings ->
        when (dataState) {
            GroupListViewModel.State.Empty -> EmptyGroupList(modifier)
            // TODO: Group list
            is GroupListViewModel.State.Groups -> GroupList(
                modifier = Modifier.padding(paddings),
                groups = dataState.groups,
                onAction = { onAction(it) }
            )
        }
    }
}

@Composable
private fun EmptyGroupList(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(1f),
        verticalArrangement = Arrangement.Center
    ) {
        Text("No groups yet created")
    }
}

@Composable
private fun GroupList(modifier: Modifier = Modifier, groups: List<Group>, onAction: (GroupItemAction) -> Unit) {
    val lazyColumnListState = rememberLazyListState()

    LazyColumn(
        modifier = modifier,
        state = lazyColumnListState
    ) {
        items(items = groups, key = { it.id }) { group ->
            Column(
                modifier = Modifier.clickable {
                    onAction(GroupItemAction.Select(group))
                }.padding(start = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(group.title)
                Text("Number of users: ${group.users.size}")
                Spacer(modifier = Modifier.height(8.dp))
                Spacer(
                    modifier = Modifier.background(MaterialTheme.colorScheme.outline).height(1.dp).fillMaxWidth(1f).padding(start = 16.dp)
                )
            }
        }
    }
}
