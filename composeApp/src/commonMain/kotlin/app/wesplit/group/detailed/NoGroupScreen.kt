package app.wesplit.group.detailed

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.empty_group_cd
import split.composeapp.generated.resources.group_detailed_empty_description
import split.composeapp.generated.resources.img_empty_group
import split.composeapp.generated.resources.img_logo_color
import split.composeapp.generated.resources.wesplit_logo

/**
 * Right pane state for expanded UI when no group selected.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun NoGroupScreen() {
    val windowSizeClass = calculateWindowSizeClass()

    Column(
        modifier = Modifier.fillMaxSize(1f).background(MaterialTheme.colorScheme.surfaceContainer).padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact) {
            Image(
                modifier = Modifier,
                painter = painterResource(Res.drawable.img_empty_group),
                contentDescription = stringResource(Res.string.empty_group_cd),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.group_detailed_empty_description),
                style = MaterialTheme.typography.bodyLarge,
            )
        } else {
            Image(
                modifier = Modifier,
                painter = painterResource(Res.drawable.img_logo_color),
                contentDescription = stringResource(Res.string.wesplit_logo),
            )
        }
    }
}
