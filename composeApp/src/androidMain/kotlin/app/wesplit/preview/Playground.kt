package app.wesplit.preview

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.currency.Amount
import app.wesplit.domain.model.currency.format
import app.wesplit.domain.model.paywall.Subscription
import app.wesplit.theme.AppTheme
import app.wesplit.ui.OrDivider
import org.jetbrains.compose.resources.painterResource
import split.composeapp.generated.resources.Res
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
        Box {
            Card(
                modifier = Modifier.padding(3.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    ),
            ) {
                products.sortedBy { (it as? Subscription)?.monthlyPrice?.value ?: 0.0 }.mapIndexed { index, subs ->
                    ListItem(
                        modifier =
                            Modifier
                                .fillMaxWidth(1f)
                                .height(IntrinsicSize.Max)
                                .padding(end = 16.dp),
                        colors =
                            ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            ),
                        overlineContent = {
                            Text(
                                text = "Try for free",
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                        headlineContent = {
                            Text(
                                text = subs.title,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        },
                        supportingContent = {
                            Text(
                                text = subs.description,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        },
                        trailingContent = {
                            Column(
                                modifier =
                                    Modifier
                                        .fillMaxHeight(1f)
                                        .width(IntrinsicSize.Max)
                                        .widthIn(min = 80.dp),
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    modifier = Modifier.fillMaxWidth(1f),
                                    text = subs.formattedPrice,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.End,
                                )
                                Text(
                                    modifier = Modifier.fillMaxWidth(1f),
                                    text = "${subs.monthlyPrice.format()}/Month",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.End,
                                )
                            }
                        },
                    )
                    if (index != products.size - 1) {
                        OrDivider()
                    }
                }
            }
            Image(
                modifier = Modifier.width(64.dp).align(Alignment.TopEnd),
                painter = painterResource(Res.drawable.img_best_offer),
                contentDescription = "Best offer badge",
            )
        }
    }

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
