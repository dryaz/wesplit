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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.back_btn_cd
import split.composeapp.generated.resources.loading

sealed interface GroupInfoAction {
    data object Back : GroupInfoAction
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun GroupInfoScreen(
    modifier: Modifier = Modifier,
    viewModel: GroupInfoViewModel,
    onAction: (GroupInfoAction) -> Unit,
) {
    val data = viewModel.dataState.collectAsState()

    val windowSizeClass = calculateWindowSizeClass()

    Scaffold(modifier = modifier, topBar = {
        if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { onAction(GroupInfoAction.Back) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.back_btn_cd),
                        )
                    }
                },
            )
        }
    }) { paddings ->
        Box(
            modifier = Modifier.padding(paddings).fillMaxSize(1f),
            contentAlignment = Alignment.Center,
        ) {
            when (val state = data.value) {
                // TODO: Proper view for group
                is GroupInfoViewModel.State.GroupInfo -> Text("Group selected: ${state.group.title}")
                GroupInfoViewModel.State.Loading -> Text(stringResource(Res.string.loading))
                // TODO: Error state
                is GroupInfoViewModel.State.Error -> Text("Error")
            }
        }
    }
}
