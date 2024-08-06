package group.detailed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

sealed interface GroupInfoAction {

    data object Back : GroupInfoAction
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun GroupInfoScreen(modifier: Modifier = Modifier, onAction: (GroupInfoAction) -> Unit) {
    val windowSizeClass = calculateWindowSizeClass()

    Scaffold(modifier = modifier, topBar = {
        if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { onAction(GroupInfoAction.Back) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                }
            )
        }
    }) { paddings ->
        Box(
            modifier = Modifier.padding(paddings).fillMaxSize(1f),
            contentAlignment = Alignment.Center
        ) {
            // TODO: Test navigation args.
            Text("Group selected")
        }
    }
}
