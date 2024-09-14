package app.wesplit.group.detailed.expense

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.empty_transaction_description
import split.composeapp.generated.resources.empty_transactions_cd
import split.composeapp.generated.resources.img_search_empty

@Composable
fun ExpenseSection(viewModel: ExpenseSectionViewModel) {
    val dataState = viewModel.dataState.collectAsState()
    when (val state = dataState.value) {
        ExpenseSectionViewModel.State.Empty -> EmptyExpenseSection(modifier = Modifier.fillMaxSize())
        is ExpenseSectionViewModel.State.Expenses -> ExpenseList(state.groupedExpenses)
        ExpenseSectionViewModel.State.Loading ->
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator()
            }

        ExpenseSectionViewModel.State.Unauthorized -> Text("Not auth") // TOOD: Non-auth state
    }
}

@Composable
private fun EmptyExpenseSection(modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxSize(1f).padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            modifier = Modifier,
            painter = painterResource(Res.drawable.img_search_empty),
            contentDescription = stringResource(Res.string.empty_transactions_cd),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.empty_transaction_description),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}
