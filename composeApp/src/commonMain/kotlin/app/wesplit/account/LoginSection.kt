package app.wesplit.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

sealed interface LoginAction {
    data object Login : LoginAction
}

@Composable
internal fun LoginSection(
    modifier: Modifier,
    onAction: (LoginAction) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize(1f),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        GoogleLoginButton { onAction(LoginAction.Login) }
    }
}
