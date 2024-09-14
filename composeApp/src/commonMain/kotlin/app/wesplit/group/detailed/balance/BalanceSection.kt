package app.wesplit.group.detailed.balance

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier

@Composable
fun BalanceSection(viewModel: BalanceSectionViewModel) {
    val dataState = viewModel.dataState.collectAsState()
    when (val state = dataState.value) {
        is BalanceSectionViewModel.State.Data -> BalanceList(state.balance)
        BalanceSectionViewModel.State.Loading ->
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator()
            }

        BalanceSectionViewModel.State.Error -> Text("Error") // TOOD: Non-auth state
    }
}
