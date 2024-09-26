package app.wesplit.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.or

@Composable
internal fun OrDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(1f).padding(horizontal = 64.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = stringResource(Res.string.or),
            style = MaterialTheme.typography.bodyMedium,
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}
