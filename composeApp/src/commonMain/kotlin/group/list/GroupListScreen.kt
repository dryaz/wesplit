package group.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.wesplit.domain.model.account.Account
import app.wesplit.domain.model.account.AccountRepository
import app.wesplit.domain.model.group.Group
import app.wesplit.domain.model.group.GroupRepository
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.CoroutineDispatcher
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.ic_split_money
import split.composeapp.generated.resources.no_image_group_cd

sealed class GroupItemAction {
    abstract val group: Group

    data class Select(
        override val group: Group,
    ) : GroupItemAction()
}

@Composable
fun GroupListScreen(
    modifier: Modifier = Modifier,
    onAction: (GroupItemAction) -> Unit,
) {
    val accountRepository: AccountRepository = koinInject()
    val groupRepository: GroupRepository = koinInject()
    val ioDispatcher: CoroutineDispatcher = koinInject()

    val viewModel: GroupListViewModel =
        viewModel {
            GroupListViewModel(
                accountRepository,
                groupRepository,
                ioDispatcher,
            )
        }

    val dataState = viewModel.dataState.collectAsState()
    val accountState = viewModel.accountState.collectAsState()

    Screen(
        modifier = modifier,
        dataState = dataState.value,
        accountState = accountState.value,
        onAction = onAction,
    )
}

// TODO: Check recomposition and probably postpone account retrivial?
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Screen(
    modifier: Modifier = Modifier,
    dataState: GroupListViewModel.State,
    accountState: Account,
    onAction: (GroupItemAction) -> Unit,
) {
    Scaffold(modifier = modifier, topBar = {
        TopAppBar(title = {
            Text(
                when (val state = accountState) {
                    is Account.Authorized -> state.name
                    Account.Unknown -> "Loading"
                    Account.Unregistered -> "Unauthorized"
                },
            )
        })
    }) { paddings ->
        when (dataState) {
            GroupListViewModel.State.Empty -> EmptyGroupList(modifier)
            // TODO: Group list
            is GroupListViewModel.State.Groups ->
                GroupList(
                    modifier = Modifier.padding(paddings),
                    groups = dataState.groups,
                    onAction = { onAction(it) },
                )
        }
    }
}

@Composable
private fun EmptyGroupList(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(1f),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("No groups yet created")
    }
}

@Composable
private fun GroupList(
    modifier: Modifier = Modifier,
    groups: List<Group>,
    onAction: (GroupItemAction) -> Unit,
) {
    val lazyColumnListState = rememberLazyListState()

    LazyColumn(
        modifier = modifier,
        state = lazyColumnListState,
    ) {
        items(items = groups, key = { it.id }) { group ->
            ListItem(
                modifier =
                    Modifier.clickable {
                        onAction(GroupItemAction.Select(group))
                    },
                headlineContent = { Text("${group.title}") },
                supportingContent = { Text("Users: ${group.users.size}") },
                leadingContent = {
                    Box(contentAlignment = Alignment.Center) {
                        val painter = rememberAsyncImagePainter(model = group.imageUrl)
                        Image(
                            painter = painter,
                            contentScale = ContentScale.Fit,
                            contentDescription = group.title,
                            modifier =
                                Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .aspectRatio(1f),
                        )
                        if (painter.state !is AsyncImagePainter.State.Success) {
                            Box(
                                modifier =
                                    Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceContainer),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_split_money),
                                    contentDescription = stringResource(Res.string.no_image_group_cd, group.title),
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                },
            )
            HorizontalDivider(modifier = Modifier.padding(start = 64.dp))
        }
    }
}
