package app.wesplit.quicksplit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.currency.Amount
import app.wesplit.domain.model.currency.format
import app.wesplit.domain.model.group.Participant
import app.wesplit.participant.ParticipantListItem
import app.wesplit.quicksplit.QuickSplitViewModel.State.Data.ShareItem
import app.wesplit.ui.AdaptiveTopAppBar
import io.github.alexzhirkevich.cupertino.adaptive.icons.AdaptiveIcons
import io.github.alexzhirkevich.cupertino.adaptive.icons.KeyboardArrowDown
import io.github.alexzhirkevich.cupertino.adaptive.icons.KeyboardArrowUp
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.collapsed
import split.composeapp.generated.resources.expanded
import split.composeapp.generated.resources.quick_split

sealed interface QuickSplitSettleAction {
    data object Back : QuickSplitSettleAction
}

// TODO: Viewmodel when it needs to add current quicksplit to existing group
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun QuickSplitSettleScreen(
    modifier: Modifier = Modifier,
    viewModel: QuickSplitViewModel,
    onAction: (QuickSplitSettleAction) -> Unit,
) {
    val data = viewModel.state.collectAsState()
    val reversedItems =
        remember(data) {
            derivedStateOf {
                (data.value as? QuickSplitViewModel.State.Data)?.items?.let {
                    QuickSplitCalculationUseCase.calculateParticipantShares(it)
                }
            }
        }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        topBar = {
            AdaptiveTopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.quick_split),
                    )
                },
                onNavigationIconClick = { onAction(QuickSplitSettleAction.Back) },
            )
        },
    ) { paddings ->
        when (val state = data.value) {
            is QuickSplitViewModel.State.Data ->
                QuickSplitSettleView(
                    modifier = Modifier.fillMaxWidth(1f).padding(paddings),
                    data = reversedItems.value,
                    currencyCode = state.amount.currencyCode,
                )

            QuickSplitViewModel.State.Loading -> CircularProgressIndicator()
        }
    }
}

@Composable
private fun QuickSplitSettleView(
    modifier: Modifier = Modifier,
    data: Map<Participant, List<ShareItem>>?,
    currencyCode: String,
) {
    Column(
        modifier = Modifier.fillMaxSize(1f).padding(top = 16.dp).verticalScroll(rememberScrollState()),
    ) {
        Card(
            modifier = modifier.fillMaxWidth(1f).padding(horizontal = 16.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
        ) {
            data?.let {
                it.mapValues { (participant, itemList) ->
                    var itemListShown by remember { mutableStateOf(false) }
                    ParticipantListItem(
                        participant = participant,
                        addAlertEnabled = false,
                        action = {
                            Row {
                                Text(
                                    text = "${Amount(value = itemList.sumOf { it.priceValue }, currencyCode = currencyCode).format()}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.secondary,
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector =
                                        if (itemListShown) {
                                            AdaptiveIcons.Outlined.KeyboardArrowUp
                                        } else {
                                            AdaptiveIcons.Outlined.KeyboardArrowDown
                                        },
                                    contentDescription =
                                        stringResource(
                                            if (itemListShown) {
                                                Res.string.expanded
                                            } else {
                                                Res.string.collapsed
                                            },
                                        ),
                                )
                            }
                        },
                        onClick = { itemListShown = !itemListShown },
                    )

                    AnimatedVisibility(
                        visible = itemListShown,
                    ) {
                        Column(
                            modifier = Modifier,
                        ) {
                            itemList.map { item ->
                                Row(
                                    modifier = Modifier.height(IntrinsicSize.Max).padding(horizontal = 32.dp),
                                ) {
                                    Column(
                                        modifier = Modifier.width(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Spacer(Modifier.weight(1f).width(2.dp).background(MaterialTheme.colorScheme.outlineVariant))
                                        Spacer(Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outlineVariant))
                                        Spacer(Modifier.weight(1f).width(2.dp).background(MaterialTheme.colorScheme.outlineVariant))
                                    }
                                    Text(
                                        modifier = Modifier.weight(1f).padding(vertical = 16.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        text = "${item.title}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        modifier = Modifier.padding(vertical = 16.dp),
                                        text = "${Amount(value = item.priceValue, currencyCode = currencyCode).format()}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.secondary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
