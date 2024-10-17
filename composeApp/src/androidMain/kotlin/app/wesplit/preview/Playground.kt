package app.wesplit.preview

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.wesplit.domain.model.currency.Amount
import app.wesplit.domain.model.currency.format
import app.wesplit.domain.model.paywall.Offer
import app.wesplit.domain.model.paywall.Subscription
import app.wesplit.theme.AppTheme
import app.wesplit.theme.extraColorScheme
import app.wesplit.ui.OrDivider
import org.jetbrains.compose.resources.painterResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.ic_badge_white
import split.composeapp.generated.resources.img_best_offer

/**
 * KMP doesn't have proper preview but android do have
 * preview mode + liveedit. So sometimes it makes sense
 * to use playground to build view.
 */
@Composable
@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
fun Playground() =
    AppTheme {
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer)) {
            Box(modifier = Modifier.padding(16.dp)) {
                PricingList(subscriptions = products) {}
            }
            Box(modifier = Modifier.padding(13.dp)) {
                PricingSelection(subscriptions = products) {}
            }
        }
    }

@Composable
private fun PricingSelection(
    modifier: Modifier = Modifier,
    subscriptions: List<Subscription>,
    onSelected: (Subscription) -> Unit,
) {
    var selected by remember {
        mutableStateOf(subscriptions.sortedBy { it.monthlyPrice.value }[0])
    }

    Column(modifier = modifier) {
        val unSelectedModifier =
            Modifier
                .padding(3.dp)
                .clip(RoundedCornerShape(15.dp))
                .border(BorderStroke(2.dp, MaterialTheme.colorScheme.primary), RoundedCornerShape(15.dp))

        val selectedModifier =
            unSelectedModifier
                .border(BorderStroke(2.dp, MaterialTheme.colorScheme.primary), RoundedCornerShape(15.dp))

        subscriptions.sortedBy { it.monthlyPrice.value }.mapIndexed { index, subscription ->
            if (index == 0) {
                Box {
                    SubscriptionItem(
                        modifier =
                            Modifier
                                .padding(3.dp)
                                .clip(RoundedCornerShape(15.dp))
                                .border(BorderStroke(2.dp, MaterialTheme.colorScheme.primary), RoundedCornerShape(15.dp)),
                        isSelected = selected == subscription,
                        subscription = subscription,
                        offer = offers[subscription.period]!!,
                    ) {
                        selected = it
                    }
                    Image(
                        modifier =
                            Modifier
                                .width(64.dp)
                                .align(Alignment.TopEnd),
                        painter = painterResource(Res.drawable.img_best_offer),
                        contentDescription = "Best offer badge",
                    )
                }
            } else {
                SubscriptionItem(
                    modifier =
                        Modifier
                            .padding(3.dp)
                            .clip(RoundedCornerShape(15.dp)),
                    isSelected = selected == subscription,
                    subscription = subscription,
                    offer = offers[subscription.period]!!,
                ) {
                    selected = it
                }
            }
            Spacer(modifier = Modifier.height(5.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        SubscriptionButton(selected = selected, offer = offers[selected.period]!!, onSelected = {})
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun PricingList(
    modifier: Modifier = Modifier,
    subscriptions: List<Subscription>,
    onSelected: (Subscription) -> Unit,
) {
    Box(modifier = modifier) {
        Card(
            modifier = Modifier.padding(3.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
        ) {
            subscriptions.sortedBy { (it as? Subscription)?.monthlyPrice?.value ?: 0.0 }.mapIndexed { index, subscription ->
                SubscriptionItem(
                    subscription = subscription,
                    offer = offers[subscription.period]!!,
                ) {
                    onSelected(subscription)
                }
                if (index != subscriptions.size - 1) {
                    OrDivider()
                }
            }
        }
        Image(
            modifier =
                Modifier
                    .width(64.dp)
                    .align(Alignment.TopEnd),
            painter = painterResource(Res.drawable.img_best_offer),
            contentDescription = "Best offer badge",
        )
    }
}

@Composable
private fun SubscriptionButton(
    selected: Subscription,
    offer: Offer,
    onSelected: (Subscription) -> Unit,
) {
    Spacer(modifier = Modifier.height(8.dp))
    FilledTonalButton(
        colors =
            ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.extraColorScheme.infoContainer,
                contentColor = MaterialTheme.extraColorScheme.onInfoContainer,
            ),
        modifier =
            Modifier
                .height(52.dp)
                .padding(4.dp)
                .padding(horizontal = 16.dp)
                .fillMaxWidth(1f),
        onClick = { onSelected(selected) },
        shape = RoundedCornerShape(10.dp),
    ) {
        Text(
            text = "Subscribe for ${selected.formattedPrice}",
            style =
                MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.7.sp,
                ),
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun SubscriptionItem(
    modifier: Modifier = Modifier,
    isSelected: Boolean = true,
    subscription: Subscription,
    offer: Offer,
    onClick: (Subscription) -> Unit,
) {
    ListItem(
        modifier =
            modifier
                .fillMaxWidth(1f)
                .height(IntrinsicSize.Max)
                .alpha(if (isSelected) 1f else 0.65f)
                .clickable {
                    onClick(subscription)
                },
        colors =
            ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            ),
        overlineContent = {
            Text(
                text = "Try for ${offer.daysFree} days",
                style = MaterialTheme.typography.labelSmall,
            )
        },
        headlineContent = {
            Row {
                Text(
                    text =
                        when (subscription.period) {
                            Subscription.Period.WEEK -> "Week Plus"
                            Subscription.Period.MONTH -> "Month Plus"
                            Subscription.Period.YEAR -> "Year Plus"
                        },
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.width(6.dp))
                androidx.compose.animation.AnimatedVisibility(visible = offer.discountPercent != 0) {
                    Box(
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            modifier = Modifier.height(24.dp),
                            painter = painterResource(Res.drawable.ic_badge_white),
                            contentDescription = "Offer badge",
                            tint = MaterialTheme.colorScheme.errorContainer,
                        )
                        Text(
                            modifier = Modifier.padding(start = 8.dp),
                            text = "- ${offer.discountPercent}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        )
                    }
                }
            }
        },
        supportingContent = {
            Text(
                text =
                    when (subscription.period) {
                        Subscription.Period.WEEK -> "Try Wesplit for 1 week"
                        Subscription.Period.MONTH -> "Use Wesplit for 1 month"
                        Subscription.Period.YEAR -> "Enjoy Wesplit for 1 year"
                    },
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        trailingContent = {
            Column(
                modifier =
                    Modifier
                        .fillMaxHeight(1f)
                        .padding(end = 8.dp)
                        .width(IntrinsicSize.Max)
                        .widthIn(min = 80.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(1f),
                    text = subscription.formattedPrice,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End,
                )
                Text(
                    modifier = Modifier.fillMaxWidth(1f),
                    text = "${subscription.monthlyPrice.format()}/Month",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End,
                )
            }
        },
    )
}

val offers =
    mapOf(
        Subscription.Period.WEEK to
            Offer(
                daysFree = 3,
                discountPercent = 0,
            ),
        Subscription.Period.MONTH to
            Offer(
                daysFree = 7,
                discountPercent = 23,
            ),
        Subscription.Period.YEAR to
            Offer(
                daysFree = 14,
                discountPercent = 58,
            ),
    )

val products =
    listOf<Subscription>(
        Subscription(
            title = "Monthly subscription",
            description = "Get full from your wesplit with montly",
            formattedPrice = "$1.99",
            monthlyPrice = Amount(1.99, "USD"),
            period = Subscription.Period.MONTH,
        ),
        Subscription(
            title = "Weekly subscription",
            description = "Get full from your wesplit with montly",
            formattedPrice = "$0.99",
            monthlyPrice = Amount(2.39, "USD"),
            period = Subscription.Period.WEEK,
        ),
        Subscription(
            title = "Yearly subscription",
            description = "Get full from your wesplit with montly",
            formattedPrice = "$19.99",
            monthlyPrice = Amount(1.39, "USD"),
            period = Subscription.Period.YEAR,
        ),
    )
