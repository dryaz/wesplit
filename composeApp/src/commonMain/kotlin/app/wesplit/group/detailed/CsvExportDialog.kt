package app.wesplit.group.detailed

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.cancel
import split.composeapp.generated.resources.csv_include_shares_description
import split.composeapp.generated.resources.csv_include_shares_title
import split.composeapp.generated.resources.csv_with_shares
import split.composeapp.generated.resources.csv_without_shares

@Composable
fun CsvExportDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onExport: (includeShares: Boolean) -> Unit,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(Res.string.csv_include_shares_title))
        },
        text = {
            Text(text = stringResource(Res.string.csv_include_shares_description))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onExport(true)
                    onDismiss()
                },
            ) {
                Text(stringResource(Res.string.csv_with_shares))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onExport(false)
                    onDismiss()
                },
            ) {
                Text(stringResource(Res.string.csv_without_shares))
            }
        },
    )
}

