package group.detailed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.group_detailed_empty_description

/**
 * Right pane state for expanded UI when no group selected.
 */
@Composable
fun NoGroupScreen() {
    Box(
        modifier = Modifier.fillMaxSize(1f),
        contentAlignment = Alignment.Center,
    ) {
        Text(stringResource(Res.string.group_detailed_empty_description))
    }
}
