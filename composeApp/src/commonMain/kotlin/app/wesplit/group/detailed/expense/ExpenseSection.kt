package app.wesplit.group.detailed.expense

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import app.wesplit.domain.model.expense.Expense
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.error

sealed interface ExpenseAction {
    data class OpenDetails(val expense: Expense) : ExpenseAction

    data class BannerClick(val banner: ExpenseSectionViewModel.Banner) : ExpenseAction
}

@Composable
fun ExpenseSection(
    viewModel: ExpenseSectionViewModel,
    onAction: (ExpenseAction) -> Unit,
) {
    val dataState = viewModel.dataState.collectAsState()
    when (val state = dataState.value) {
        ExpenseSectionViewModel.State.Empty -> EmptyExpenseSection(modifier = Modifier.fillMaxSize())
        is ExpenseSectionViewModel.State.Expenses -> ExpenseList(state.group, state.groupedExpenses, state.banner, onAction)
        ExpenseSectionViewModel.State.Loading ->
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator()
            }

        ExpenseSectionViewModel.State.Error -> Text(stringResource(Res.string.error)) // TOOD: Non-auth state
    }
}
