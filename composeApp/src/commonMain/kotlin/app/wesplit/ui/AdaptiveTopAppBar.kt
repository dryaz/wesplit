package app.wesplit.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.back
import split.composeapp.generated.resources.back_btn_cd

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveTopAppBar(
    title: @Composable () -> Unit = {},
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    navigationIcon: @Composable () -> Unit = {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(Res.string.back_btn_cd),
        )
    },
    navigationTitle: @Composable () -> Unit = {
        Text(
            text = stringResource(Res.string.back),
            style = MaterialTheme.typography.labelSmall,
        )
    },
    onNavigationIconClick: (() -> Unit)? = null,
) {
    val windowSizeClass = calculateWindowSizeClass()

    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
        TopAppBar(
            modifier = modifier,
            title = title,
            navigationIcon = {
                onNavigationIconClick?.let { action ->
                    IconButton(onClick = { action() }) {
                        navigationIcon()
                    }
                }
            },
            actions = actions,
        )
    } else {
        CenterAlignedTopAppBar(
            modifier = modifier,
            title = title,
            navigationIcon = {
                onNavigationIconClick?.let { action ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxHeight(1f)
                                .clickable {
                                    action()
                                }.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        navigationIcon()
                        Spacer(modifier = Modifier.width(8.dp))
                        navigationTitle()
                    }
                }
            },
            actions = actions,
        )
    }
}
