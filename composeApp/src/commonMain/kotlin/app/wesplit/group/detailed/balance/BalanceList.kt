package app.wesplit.group.detailed.balance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.currency.format
import app.wesplit.domain.model.group.Balance
import app.wesplit.domain.model.group.BalanceStatus
import app.wesplit.domain.model.group.Participant
import app.wesplit.domain.model.group.isConnected
import app.wesplit.group.detailed.checkBalanceTutorialStepFlow
import app.wesplit.participant.ParticipantListItem
import app.wesplit.ui.tutorial.LocalTutorialControl
import app.wesplit.ui.tutorial.TutorialItem
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveCircularProgressIndicator
import io.github.alexzhirkevich.cupertino.adaptive.ExperimentalAdaptiveApi
import io.github.alexzhirkevich.cupertino.adaptive.icons.AdaptiveIcons
import io.github.alexzhirkevich.cupertino.adaptive.icons.Email
import io.github.alexzhirkevich.cupertino.adaptive.icons.Refresh
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.data_syncing
import split.composeapp.generated.resources.get_plus_instant_balances
import split.composeapp.generated.resources.ic_flag
import split.composeapp.generated.resources.ic_plus
import split.composeapp.generated.resources.invite_user
import split.composeapp.generated.resources.offline_balances_shown
import split.composeapp.generated.resources.plus_badge
import split.composeapp.generated.resources.recalculating_backend
import split.composeapp.generated.resources.settle_balances
import split.composeapp.generated.resources.sync_in_progress
import split.composeapp.generated.resources.undistributed

private const val BACKED_CALCULATION_CLICK = "plus_recalculate_click"

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BalanceList(
    balance: Balance?,
    onInvite: (Participant) -> Unit,
    onSettle: () -> Unit,
) {
    val tutorialControl = LocalTutorialControl.current
    val analyticsManager: AnalyticsManager = koinInject()

    if (balance != null) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
        ) {
            TutorialItem(
                onPositioned = { tutorialControl.onPositionRecieved(checkBalanceTutorialStepFlow[1], it) },
                suffixModifier = Modifier.padding(bottom = 64.dp),
            ) { modifier ->
                Card(
                    modifier =
                        modifier
                            .fillMaxWidth(1f)
                            .padding(16.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        ),
                ) {
                    balance.participantsBalance.forEach { balanceItem ->
                        val action: @Composable (() -> Unit)? =
                            if (!balanceItem.participant.isConnected()) {
                                {
                                    FilledIconButton(
                                        colors =
                                            IconButtonDefaults.filledIconButtonColors(
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            ),
                                        shape = CircleShape,
                                        onClick = {
                                            onInvite(balanceItem.participant)
                                        },
                                    ) {
                                        Icon(
                                            AdaptiveIcons.Outlined.Email,
                                            contentDescription = stringResource(Res.string.invite_user),
                                        )
                                    }
                                }
                            } else {
                                null
                            }

                        val callback: ((Participant) -> Unit)? =
                            if (!balanceItem.participant.isConnected()) {
                                { participant -> onInvite(participant) }
                            } else {
                                null
                            }

                        ParticipantListItem(
                            modifier = if (balance.status == BalanceStatus.INVALID) Modifier.alpha(0.4f) else Modifier,
                            action = action,
                            onClick = callback,
                            participant = balanceItem.participant,
                            subComposable = {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(1f),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    balanceItem.amounts.filter { it.value != 0.0 }.forEach { amount ->
                                        FilterChip(
                                            selected = false,
                                            onClick = {},
                                            enabled = false,
                                            label = { Text(amount.format()) },
                                            colors =
                                                FilterChipDefaults.filterChipColors(
                                                    disabledContainerColor =
                                                        if (amount.value > 0.0) {
                                                            MaterialTheme.colorScheme.secondaryContainer
                                                        } else {
                                                            MaterialTheme.colorScheme.error
                                                        },
                                                    disabledLabelColor =
                                                        if (amount.value > 0.0) {
                                                            MaterialTheme.colorScheme.onSecondaryContainer
                                                        } else {
                                                            MaterialTheme.colorScheme.onError
                                                        },
                                                ),
                                        )
                                    }
                                }
                            },
                        )
                    }

                    if (balance.undistributed.isNotEmpty()) {
                        HorizontalDivider()
                        Undistributed(balance)
                    }

                    AnimatedVisibility(
                        visible = balance.status == BalanceStatus.INVALID,
                    ) {
                        HorizontalDivider()
                        Invalid {
                            analyticsManager.track(BACKED_CALCULATION_CLICK)
                        }
                    }

                    AnimatedVisibility(
                        visible = balance.status == BalanceStatus.LOCAL,
                    ) {
                        HorizontalDivider()
                        LocalBalances()
                    }

                    TutorialItem(
                        onPositioned = { tutorialControl.onPositionRecieved(checkBalanceTutorialStepFlow[2], it) },
                    ) { modifier ->
                        AnimatedVisibility(
                            modifier = modifier.fillMaxWidth(1f),
                            visible = balance.participantsBalance.any { it.amounts.any { it.value != 0.0 } },
                        ) {
                            ListItem(
                                modifier =
                                    Modifier.fillMaxWidth().clickable {
                                        if (balance.status == BalanceStatus.SYNC) onSettle()
                                    }.then(
                                        if (balance.status == BalanceStatus.SYNC) Modifier else Modifier.alpha(0.4f),
                                    ),
                                colors =
                                    ListItemDefaults.colors(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        headlineColor = MaterialTheme.colorScheme.onSecondary,
                                    ),
                                headlineContent = {
                                    Text(
                                        modifier = Modifier.fillMaxSize(1f),
                                        text = stringResource(Res.string.settle_balances),
                                        textAlign = TextAlign.Center,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    } else {
        SettledBalances()
    }
}

@OptIn(ExperimentalAdaptiveApi::class)
@Composable
fun Invalid(onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        colors =
            ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            ),
        headlineContent = {
            Text(
                text = stringResource(Res.string.recalculating_backend),
            )
        },
        leadingContent = {
            AdaptiveCircularProgressIndicator()
        },
        trailingContent = {
            Image(
                modifier = Modifier.height(24.dp),
                painter = painterResource(Res.drawable.ic_plus),
                contentDescription = stringResource(Res.string.plus_badge),
            )
        },
        supportingContent = {
            Text(
                text = stringResource(Res.string.get_plus_instant_balances),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
            )
        },
    )
}

@Composable
fun LocalBalances() {
    ListItem(
        colors =
            ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            ),
        headlineContent = {
            Text(
                text = stringResource(Res.string.offline_balances_shown),
            )
        },
        leadingContent = {
            Icon(
                imageVector = AdaptiveIcons.Outlined.Refresh,
                contentDescription = stringResource(Res.string.data_syncing),
            )
        },
        trailingContent = {
            Image(
                modifier = Modifier.height(24.dp),
                painter = painterResource(Res.drawable.ic_plus),
                contentDescription = stringResource(Res.string.plus_badge),
            )
        },
        supportingContent = {
            Text(
                text = stringResource(Res.string.sync_in_progress),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
            )
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Undistributed(balance: Balance) {
    ListItem(
        modifier = if (balance.status == BalanceStatus.INVALID) Modifier.alpha(0.4f) else Modifier,
        colors =
            ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            ),
        headlineContent = {
            Text(
                text = stringResource(Res.string.undistributed),
            )
        },
        leadingContent = {
            Icon(
                modifier = Modifier.width(56.dp),
                painter = painterResource(Res.drawable.ic_flag),
                contentDescription = stringResource(Res.string.undistributed),
            )
        },
        supportingContent = {
            FlowRow(
                modifier = Modifier.fillMaxWidth(1f),
            ) {
                balance.undistributed.forEach { amount ->
                    SuggestionChip(
                        onClick = {},
                        label = { Text(amount.format()) },
                    )
                }
            }
        },
    )
}

@Composable
private fun SettledBalances() {
    Text(stringResource(Res.string.settle_balances))
}
