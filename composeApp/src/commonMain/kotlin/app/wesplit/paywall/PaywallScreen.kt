package app.wesplit.paywall

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.wesplit.domain.model.account.Account
import app.wesplit.domain.model.account.AccountRepository
import app.wesplit.domain.model.currency.format
import app.wesplit.domain.model.paywall.Subscription
import app.wesplit.domain.model.user.Plan
import app.wesplit.theme.extraColorScheme
import app.wesplit.ui.AdaptiveTopAppBar
import app.wesplit.ui.OrDivider
import app.wesplit.ui.PlusProtected
import io.github.alexzhirkevich.cupertino.adaptive.icons.AdaptiveIcons
import io.github.alexzhirkevich.cupertino.adaptive.icons.Done
import io.github.alexzhirkevich.cupertino.adaptive.icons.Info
import io.github.alexzhirkevich.cupertino.adaptive.icons.KeyboardArrowLeft
import io.github.alexzhirkevich.cupertino.adaptive.icons.Lock
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.back_btn_cd
import split.composeapp.generated.resources.ic_mobile_app
import split.composeapp.generated.resources.ic_plus
import split.composeapp.generated.resources.img_best_offer
import split.composeapp.generated.resources.img_feature_protect

sealed interface PaywallAction {
    data object Back : PaywallAction

    data object DownloadMobile : PaywallAction
}

@Composable
fun PaywallRoute(
    modifier: Modifier = Modifier,
    viewModel: PaywallViewModel,
    onAction: (PaywallAction) -> Unit,
) {
    val accountRepository: AccountRepository = koinInject()
    val accountState = accountRepository.get().collectAsState()
    val productsState = viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            AdaptiveTopAppBar(
                navigationIcon = {
                    Icon(
                        imageVector = AdaptiveIcons.Outlined.KeyboardArrowLeft,
                        contentDescription = stringResource(Res.string.back_btn_cd),
                    )
                },
                onNavigationIconClick = { onAction(PaywallAction.Back) },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Benefits",
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Image(
                            modifier = Modifier.height(24.dp),
                            painter = painterResource(Res.drawable.ic_plus),
                            contentDescription = "Plus badge for benefits",
                        )
                    }
                },
            )
        },
    ) { paddings ->
        PaywallScreen(
            modifier = Modifier.padding(paddings),
            account = accountState.value,
            productState = productsState.value,
            onAction = onAction,
        ) {
            viewModel.subscribe(it)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.event.collectLatest { event ->
            when (event) {
                is PaywallViewModel.Event.Error -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(event.msg)
                    }
                }
                PaywallViewModel.Event.Purchased -> onAction(PaywallAction.Back)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PaywallScreen(
    modifier: Modifier = Modifier,
    account: Account,
    productState: PaywallViewModel.State,
    onAction: (PaywallAction) -> Unit,
    onSubscribe: (Subscription) -> Unit,
) {
    val trailingIcon =
        if ((account as? Account.Authorized)?.user?.plan == Plan.BASIC) {
            AdaptiveIcons.Outlined.Lock
        } else {
            AdaptiveIcons.Outlined.Done
        }

    var selected by remember {
        mutableStateOf<Subscription?>(null)
    }

    LaunchedEffect(productState) {
        selected = (productState as? PaywallViewModel.State.Data)?.products?.sortedBy { it.monthlyPrice.value }?.get(0)
    }

    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        Row {
            when (productState) {
                is PaywallViewModel.State.Data -> {
                    selected?.let {
                        PricingSelection(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            subscriptions = productState.products,
                            selected = it,
                        ) {
                            selected = it
                        }
                    }
                }

                PaywallViewModel.State.Error -> {
                    ListItem(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).clip(RoundedCornerShape(15.dp)),
                        colors =
                            ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                headlineColor = MaterialTheme.colorScheme.onErrorContainer,
                                leadingIconColor = MaterialTheme.colorScheme.onErrorContainer,
                                supportingColor = MaterialTheme.colorScheme.onErrorContainer,
                            ),
                        headlineContent = { Text("Error fetching plans") },
                        supportingContent = { Text("We encountered problems, please try again later") },
                        leadingContent = {
                            Icon(
                                imageVector = AdaptiveIcons.Outlined.Info,
                                contentDescription = "We encountered problems, please try again later",
                            )
                        },
                    )
                }

                PaywallViewModel.State.Loading -> {
                    ListItem(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).clip(RoundedCornerShape(15.dp)),
                        colors =
                            ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            ),
                        headlineContent = { Text("Loading...") },
                        supportingContent = { Text("Fetch all available plans") },
                        leadingContent = { CircularProgressIndicator() },
                    )
                }

                PaywallViewModel.State.BillingNotSupported -> {
                    ListItem(
                        modifier =
                            Modifier.padding(horizontal = 16.dp, vertical = 8.dp).clip(RoundedCornerShape(15.dp)).clickable {
                                onAction(PaywallAction.DownloadMobile)
                            },
                        colors =
                            ListItemDefaults.colors(
                                containerColor = MaterialTheme.extraColorScheme.infoContainer,
                                headlineColor = MaterialTheme.extraColorScheme.onInfoContainer,
                                leadingIconColor = MaterialTheme.extraColorScheme.onInfoContainer,
                                supportingColor = MaterialTheme.extraColorScheme.onInfoContainer,
                            ),
                        headlineContent = { Text("Billing not yet done :(") },
                        supportingContent = { Text("Use our mobile apps instead now") },
                        leadingContent = {
                            Icon(
                                painter = painterResource(Res.drawable.ic_mobile_app),
                                contentDescription = "Get mobile application",
                            )
                        },
                    )
                }

                PaywallViewModel.State.AlreadySubscribed -> {
                    ListItem(
                        modifier =
                            Modifier.padding(horizontal = 16.dp, vertical = 8.dp).clip(RoundedCornerShape(15.dp)).clickable {
                                onAction(PaywallAction.DownloadMobile)
                            },
                        colors =
                            ListItemDefaults.colors(
                                containerColor = MaterialTheme.extraColorScheme.infoContainer,
                                headlineColor = MaterialTheme.extraColorScheme.onInfoContainer,
                                leadingIconColor = MaterialTheme.extraColorScheme.onInfoContainer,
                                supportingColor = MaterialTheme.extraColorScheme.onInfoContainer,
                            ),
                        headlineContent = { Text("Your plus subscription") },
                        supportingContent = { Text("Enjoy all features in Wesplit") },
                        leadingContent = {
                            Icon(
                                imageVector = AdaptiveIcons.Outlined.Done,
                                contentDescription = "Plus plan activated",
                            )
                        },
                    )
                }
            }
        }

        selected?.let {
            SubscriptionButton(it) {
                onSubscribe(it)
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth(1f).padding(16.dp),
        ) {
            PlusProtected(
                modifier = Modifier.align(Alignment.Center),
            ) {
                Text(
                    modifier = Modifier,
                    text = "What you get with",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )
            }
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth(1f).padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            features.map { feature ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Card(
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            ),
                        modifier = Modifier.widthIn(max = 360.dp),
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.img_feature_protect),
                            contentDescription = stringResource(feature.title),
                        )
                        ListItem(
                            modifier = Modifier.fillMaxWidth(1f),
                            colors =
                                ListItemDefaults.colors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                ),
                            trailingContent = {
                                Icon(
                                    modifier = Modifier.minimumInteractiveComponentSize(),
                                    imageVector = trailingIcon,
                                    contentDescription = "Locked feature",
                                )
                            },
                            headlineContent = {
                                Text(
                                    text = stringResource(feature.title),
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = stringResource(feature.shortDescr),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.outline,
                                )
                            },
                        )

                        Text(
                            modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp),
                            text = stringResource(feature.fullDescr),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        selected?.let {
            SubscriptionButton(it) {
                onSubscribe(it)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PricingSelection(
    modifier: Modifier = Modifier,
    subscriptions: List<Subscription>,
    selected: Subscription,
    onSelected: (Subscription) -> Unit,
) {
    val unSelectedModifier =
        Modifier
            .padding(3.dp)
            .clip(RoundedCornerShape(15.dp))

    val selectedModifier =
        unSelectedModifier
            .border(BorderStroke(2.dp, MaterialTheme.colorScheme.primary), RoundedCornerShape(15.dp))

    Column(modifier = modifier) {
        subscriptions.sortedBy { it.monthlyPrice.value }.mapIndexed { index, subscription ->
            if (index == 0) {
                Box {
                    SubscriptionItem(
                        modifier = if (selected == subscription) selectedModifier else unSelectedModifier,
                        isSelected = selected == subscription,
                        subscription = subscription,
                    ) {
                        onSelected(it)
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
                    modifier = if (selected == subscription) selectedModifier else unSelectedModifier,
                    isSelected = selected == subscription,
                    subscription = subscription,
                ) {
                    onSelected(it)
                }
            }
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}

@Composable
private fun SubscriptionButton(
    selected: Subscription,
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
@Deprecated("PricingSelection is used, this one could be usde for AB test")
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
                SubscriptionItem(subscription = subscription) {
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
private fun SubscriptionItem(
    modifier: Modifier = Modifier,
    isSelected: Boolean = true,
    subscription: Subscription,
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
                text = "Try for free",
                style = MaterialTheme.typography.labelSmall,
            )
        },
        headlineContent = {
            Text(
                text =
                    when (subscription.period) {
                        Subscription.Period.WEEK -> "Week Plus"
                        Subscription.Period.MONTH -> "Month Plus"
                        Subscription.Period.YEAR -> "Year Plus"
                    },
                style = MaterialTheme.typography.titleMedium,
            )
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
