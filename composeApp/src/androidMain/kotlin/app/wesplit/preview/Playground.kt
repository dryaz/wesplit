package app.wesplit.preview

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

/**
 * KMP doesn't have proper preview but android do have
 * preview mode + liveedit. So sometimes it makes sense
 * to use playground to build view.
 *
 * NB: AppTheme can't be used here 'cause it inects custom fonts
 * Maybe it make sense to create custom theme with default fonts
 * in order to support at least colors.
 */
@Composable
@Preview(showSystemUi = true, widthDp = 900)
fun Playground() =
    MaterialTheme {
    }
