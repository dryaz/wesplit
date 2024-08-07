package group.detailed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Right pane state for expanded UI when no group selected.
 */
@Composable
fun NoGroupScreen() {
    Box(
        modifier = Modifier.fillMaxSize(1f),
        contentAlignment = Alignment.Center,
    ) {
        Text("No group selected")
    }
}
