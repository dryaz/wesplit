package app.wesplit.preview

import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.wesplit.theme.AppTheme

/**
 * KMP doesn't have proper preview but android do have
 * preview mode + liveedit. So sometimes it makes sense
 * to use playground to build view.
 */
@Composable
@Preview(showSystemUi = true)
fun Playground() =
    AppTheme {
        GroupTabs()
    }

@Composable
private fun GroupTabs() {
    TabRow(selectedTabIndex = 2) {
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
