package previews

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import app.wesplit.domain.model.account.Account
import app.wesplit.domain.model.group.Group
import group.list.GroupListScreen
import group.list.GroupListViewModel
import theme.AppTheme

@Preview
@Composable
fun GroupScreenListEmptyUnauthPreview() =
    AppTheme {
        GroupListScreen(
            dataState = GroupListViewModel.State.Empty,
            accountState = Account.Unregistered,
            onAction = {},
        )
    }

@Preview
@Composable
fun GroupScreenListNonEmptyUnauthPreview() =
    AppTheme {
        GroupListScreen(
            dataState =
                GroupListViewModel.State.Groups(
                    listOf(Group("Id", "Title", null, emptyList())),
                ),
            accountState = Account.Unregistered,
            onAction = {},
        )
    }

@Preview
@Composable
fun GroupScreenListUnkownPreview() =
    AppTheme {
        GroupListScreen(
            dataState = GroupListViewModel.State.Empty,
            accountState = Account.Unknown,
            onAction = {},
        )
    }
