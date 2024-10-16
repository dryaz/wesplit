package app.wesplit.group.detailed.balance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.currency.format
import app.wesplit.domain.model.group.Balance
import app.wesplit.domain.model.group.Participant
import app.wesplit.domain.model.group.isConnected
import app.wesplit.group.detailed.checkBalanceTutorialStepFlow
import app.wesplit.participant.ParticipantListItem
import app.wesplit.ui.tutorial.LocalTutorialControl
import app.wesplit.ui.tutorial.TutorialItem
import io.github.alexzhirkevich.cupertino.adaptive.icons.AdaptiveIcons
import io.github.alexzhirkevich.cupertino.adaptive.icons.Done
import io.github.alexzhirkevich.cupertino.adaptive.icons.Email
import org.jetbrains.compose.resources.painterResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.ic_flag

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BalanceList(
    balance: Balance?,
    onInvite: (Participant) -> Unit,
    onSettle: () -> Unit,
) {
    var settleDialogShown by remember { mutableStateOf(false) }
    val tutorialControl = LocalTutorialControl.current

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
                                            contentDescription = "Invite user",
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
                                        settleDialogShown = true
                                    },
                                colors =
                                    ListItemDefaults.colors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        headlineColor = MaterialTheme.colorScheme.onPrimary,
                                    ),
                                headlineContent = {
                                    Text(
                                        modifier = Modifier.fillMaxSize(1f),
                                        text = "Settle balances",
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

    if (settleDialogShown) {
        AlertDialog(
            modifier = Modifier.widthIn(max = 450.dp),
            onDismissRequest = { settleDialogShown = false },
            title = { Text("Settle all balances?") },
            text = {
                Text(
                    text = "Are you sure that you want to\n mark all current expenses as settled?",
                    textAlign = TextAlign.Center,
                )
            },
            icon = {
                Icon(
                    AdaptiveIcons.Outlined.Done,
                    contentDescription = "Settled all expenses",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSettle()
                        settleDialogShown = false
                    },
                ) {
                    Text(
                        text = "Yes, Settle",
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        settleDialogShown = false
                    },
                ) {
                    Text(
                        text = "No, Wait",
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Undistributed(balance: Balance) {
    ListItem(
        colors =
            ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            ),
        headlineContent = {
            Text(
                text = "Undistributed",
            )
        },
        leadingContent = {
            Icon(
                modifier = Modifier.width(56.dp),
                painter = painterResource(Res.drawable.ic_flag),
                contentDescription = "Undistributed",
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
    Text("Settled balances here")
}
