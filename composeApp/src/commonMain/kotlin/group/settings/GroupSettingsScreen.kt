package group.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.create
import split.composeapp.generated.resources.loading
import split.composeapp.generated.resources.new_group
import split.composeapp.generated.resources.retry
import split.composeapp.generated.resources.save
import split.composeapp.generated.resources.settings
import ui.AdaptiveTopAppBar

sealed interface GroupSettingsAction {
    data object Back : GroupSettingsAction
}

private sealed interface GroupSettingTollbarAction {
    data object Reload : GroupSettingTollbarAction

    data object Commit : GroupSettingTollbarAction
}

@Composable
fun GroupSettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: GroupSettingsViewModel,
    onAction: (GroupSettingsAction) -> Unit,
) {
    val state = viewModel.state.collectAsState()

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBareByState(
                state = state.value,
                onAction = onAction,
                onToolbarAction = { action ->
                    when (action) {
                        GroupSettingTollbarAction.Commit -> {
                            viewModel.commit()
                            onAction(GroupSettingsAction.Back)
                        }

                        GroupSettingTollbarAction.Reload -> viewModel.reload()
                    }
                },
            )
        },
    ) { paddings ->
        Text(
            modifier = Modifier.padding(paddings),
            text = "Here group info",
        )
    }
}

@Composable
private fun TopAppBareByState(
    state: GroupSettingsViewModel.State,
    onAction: (GroupSettingsAction) -> Unit,
    onToolbarAction: (GroupSettingTollbarAction) -> Unit,
) {
    AdaptiveTopAppBar(
        title = {
            Text(
                stringResource(
                    when (state) {
                        GroupSettingsViewModel.State.Loading -> Res.string.loading
                        is GroupSettingsViewModel.State.Error -> Res.string.settings
                        is GroupSettingsViewModel.State.Group ->
                            if (state.id == null) {
                                Res.string.new_group
                            } else {
                                Res.string.settings
                            }
                    },
                ),
            )
        },
        onBack = { onAction(GroupSettingsAction.Back) },
        actions = {
            Box(
                modifier = Modifier.fillMaxHeight(1f).clickable {
                    when (state) {
                        is GroupSettingsViewModel.State.Error -> onToolbarAction(GroupSettingTollbarAction.Reload)
                        is GroupSettingsViewModel.State.Group -> onToolbarAction(GroupSettingTollbarAction.Commit)
                        GroupSettingsViewModel.State.Loading -> {}
                    }
                }.padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                when (state) {
                    is GroupSettingsViewModel.State.Error ->
                        Text(
                            // TODO: Add leading icon retry icon
                            text = stringResource(Res.string.retry),
                        )

                    is GroupSettingsViewModel.State.Group ->
                        if (state.id == null) {
                            // TODO: Add leading icon OK
                            Text(
                                text = stringResource(Res.string.create),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        } else {
                            Text(
                                text = stringResource(Res.string.save),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }

                    GroupSettingsViewModel.State.Loading -> CircularProgressIndicator()
                }
            }
        },
    )
}
