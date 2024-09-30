package app.wesplit.account

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AnonymousLoginButton(onAction: () -> Unit) {
    var isLoading by remember { mutableStateOf(false) }
    OutlinedButton(
        onClick = {
            isLoading = true
            onAction()
        },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Login Anonymously",
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
