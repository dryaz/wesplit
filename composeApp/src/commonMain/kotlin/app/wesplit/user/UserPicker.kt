package app.wesplit.user

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.wesplit.domain.model.user.User

// TODO: Ux improvement - add multiple users from single bottom sheet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun UserPicker(
    viewModel: UserPickerViewModel =
        viewModel {
            UserPickerViewModel()
        },
    onUserSelect: (User?) -> Unit,
) {
    // TODO: Search user, last option is to create new user with name as current input in search
    // TODO: Empty state, if no yet users show img and description that start typing to create contact
    // TODO: Platform delegate to request access to contacts to get contacts
    // TODO: Good point for linkedin post how to create user picker in KMP compose
    var searchText by remember { mutableStateOf("") }
    val lazyColumnListState = rememberLazyListState()

    val users =
        (0..50).map { num ->
            User(
                "$num",
                "User #$num",
                "",
            )
        }

    ModalBottomSheet(
        onDismissRequest = { onUserSelect(null) },
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = lazyColumnListState,
        ) {
            items(items = users, key = { it.name }) { user ->
                UserListItem(user) {
                    onUserSelect(it)
                }
                HorizontalDivider(modifier = Modifier.padding(start = 64.dp))
            }
        }
    }

    // TODO: Fire callback after modal is closed first to improve UX
}
