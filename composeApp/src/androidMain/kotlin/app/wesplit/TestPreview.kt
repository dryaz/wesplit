package app.wesplit

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.wesplit.domain.model.account.Account
import group.list.GroupListScreen
import group.list.GroupListViewModel
import theme.AppTheme

@Preview
@Composable
fun TestPreview() =
    AppTheme {
        GroupListScreen(dataState = GroupListViewModel.State.Empty, accountState = Account.Unknown) {
        }
    }
