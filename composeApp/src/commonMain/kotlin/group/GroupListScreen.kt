package group

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GroupListScreen(viewModel: GroupListViewModel = koinViewModel()) {
    Text(viewModel.get())
}
