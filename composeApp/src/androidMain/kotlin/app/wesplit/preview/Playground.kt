package app.wesplit.preview

import android.content.res.Configuration
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
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.currency.Amount
import app.wesplit.domain.model.currency.format
import app.wesplit.domain.model.paywall.Offer
import app.wesplit.domain.model.paywall.Subscription
import app.wesplit.theme.extraColorScheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.days
import split.composeapp.generated.resources.free
import split.composeapp.generated.resources.ic_badge_white
import split.composeapp.generated.resources.month_plus
import split.composeapp.generated.resources.offer_badge
import split.composeapp.generated.resources.per_month
import split.composeapp.generated.resources.per_week
import split.composeapp.generated.resources.per_year
import split.composeapp.generated.resources.try_days
import split.composeapp.generated.resources.try_month_plus
import split.composeapp.generated.resources.try_week_plus
import split.composeapp.generated.resources.try_year_plus
import split.composeapp.generated.resources.week_plus
import split.composeapp.generated.resources.year_plus

/**
 * KMP doesn't have proper preview but android do have
 * preview mode + liveedit. So sometimes it makes sense
 * to use playground to build view.
 */
@Composable
@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
fun Playground() {
    Column {
        PriceFocusedItem(
            modifier = Modifier,
            isSelected = false,
            onClick = {},
            subscription = Subscription("title", "descr", "$20.49", Amount(20.0, "USD"), Subscription.Period.YEAR),
            offer = Offer(14, 50),
        )

        FreeTierFocusedItem(
            modifier = Modifier,
            isSelected = false,
            onClick = {},
            subscription = Subscription("title", "descr", "$20.49", Amount(20.0, "USD"), Subscription.Period.YEAR),
            offer = Offer(3, 50),
        )
    }
}

@Composable
private fun FreeTierFocusedItem(
    modifier: Modifier,
    isSelected: Boolean,
    onClick: (Subscription) -> Unit,
    subscription: Subscription,
    offer: Offer,
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
        headlineContent = {
            Row {
                Text(
                    text =
                        when (subscription.period) {
                            Subscription.Period.WEEK -> stringResource(Res.string.week_plus)
                            Subscription.Period.MONTH -> stringResource(Res.string.month_plus)
                            Subscription.Period.YEAR -> stringResource(Res.string.year_plus)
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
                            contentDescription = stringResource(Res.string.offer_badge),
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
                modifier = Modifier.fillMaxWidth(1f),
                text =
                    when (subscription.period) {
                        Subscription.Period.WEEK -> stringResource(Res.string.per_week, subscription.formattedPrice)
                        Subscription.Period.MONTH -> stringResource(Res.string.per_month, subscription.formattedPrice)
                        Subscription.Period.YEAR -> stringResource(Res.string.per_year, subscription.formattedPrice)
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
                    text = pluralStringResource(Res.plurals.days, offer.daysFree),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.extraColorScheme.infoContainer,
                    textAlign = TextAlign.Center,
                )
                Text(
                    modifier = Modifier.fillMaxWidth(1f),
                    text = stringResource(Res.string.free),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.extraColorScheme.infoContainer,
                    textAlign = TextAlign.Center,
                )
            }
        },
    )
}

@Composable
private fun PriceFocusedItem(
    modifier: Modifier,
    isSelected: Boolean,
    onClick: (Subscription) -> Unit,
    subscription: Subscription,
    offer: Offer,
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
                text = stringResource(Res.string.try_days, offer.daysFree),
                style = MaterialTheme.typography.labelSmall,
            )
        },
        headlineContent = {
            Row {
                Text(
                    text =
                        when (subscription.period) {
                            Subscription.Period.WEEK -> stringResource(Res.string.week_plus)
                            Subscription.Period.MONTH -> stringResource(Res.string.month_plus)
                            Subscription.Period.YEAR -> stringResource(Res.string.year_plus)
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
                            contentDescription = stringResource(Res.string.offer_badge),
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
                        Subscription.Period.WEEK -> stringResource(Res.string.try_week_plus)
                        Subscription.Period.MONTH -> stringResource(Res.string.try_month_plus)
                        Subscription.Period.YEAR -> stringResource(Res.string.try_year_plus)
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
                    text = stringResource(Res.string.per_month, subscription.monthlyPrice.format()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End,
                )
            }
        },
    )
}
