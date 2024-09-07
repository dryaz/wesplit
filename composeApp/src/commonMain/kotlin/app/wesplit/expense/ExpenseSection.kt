package app.wesplit.expense

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier

@Composable
fun ExpenseSection(viewModel: ExpenseSectionViewModel) {
    val dataState = viewModel.dataState.collectAsState()
    when (val state = dataState.value) {
        ExpenseSectionViewModel.State.Empty -> Text("Empty") // TODO: Empty state
        is ExpenseSectionViewModel.State.Expenses -> ExpenseList(state.groupedExpenses)
        ExpenseSectionViewModel.State.Loading ->
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator()
            }

        ExpenseSectionViewModel.State.Unauthorized -> Text("Not auth") // TOOD: Non-auth state
    }
}
