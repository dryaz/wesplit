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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import app.wesplit.KotlinPlatform
import app.wesplit.currentPlatform
import app.wesplit.domain.model.currency.format
import app.wesplit.domain.model.paywall.Offer
import app.wesplit.domain.model.paywall.Subscription
import app.wesplit.domain.model.user.User
import app.wesplit.domain.model.user.UserRepository
import app.wesplit.domain.model.user.isPlus
import app.wesplit.theme.extraColorScheme
import app.wesplit.ui.AdaptiveTopAppBar
import app.wesplit.ui.PlusProtected
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveCircularProgressIndicator
import io.github.alexzhirkevich.cupertino.adaptive.ExperimentalAdaptiveApi
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
import split.composeapp.generated.resources.ic_badge_white
import split.composeapp.generated.resources.ic_mobile_app
import split.composeapp.generated.resources.ic_plus
import split.composeapp.generated.resources.ic_promo
import split.composeapp.generated.resources.img_best_offer

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
    val userRepository: UserRepository = koinInject()
    val userState = userRepository.get().collectAsState()
    val productsState = viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isPendingPurchase by remember { mutableStateOf(false) }
    var showPromoDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
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
                actions = {
                    if (!userState.value.isPlus() && currentPlatform !is KotlinPlatform.Mobile) {
                        IconButton(
                            modifier = modifier,
                            onClick = { showPromoDialog = true },
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_promo),
                                contentDescription = "Enter promocode",
                            )
                        }
                    } else {
                        Unit
                    }
                },
            )
        },
    ) { paddings ->
        PaywallScreen(
            modifier = Modifier.padding(paddings),
            user = userState.value,
            productState = productsState.value,
            isPendingPurchase = isPendingPurchase,
            onAction = onAction,
        ) {
            isPendingPurchase = true
            viewModel.subscribe(it)
        }
    }

    if (showPromoDialog) {
        var promoValue by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPromoDialog = false },
            title = {
                Text(text = "Enter Promocode")
            },
            text = {
                Column {
                    Text(text = "Enter valid Promocode")
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = promoValue,
                        onValueChange = { promoValue = it },
                        label = { Text("Code") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isPendingPurchase = true
                        showPromoDialog = false
                        viewModel.applyPromocode(promoValue)
                    },
                    enabled = promoValue.isNotBlank(),
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPromoDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    LaunchedEffect(Unit) {
        viewModel.event.collectLatest { event ->
            when (event) {
                is PaywallViewModel.Event.Error -> {
                    scope.launch {
                        isPendingPurchase = false
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
    user: User?,
    productState: PaywallViewModel.State,
    isPendingPurchase: Boolean,
    onAction: (PaywallAction) -> Unit,
    onSubscribe: (Subscription) -> Unit,
) {
    val trailingIcon =
        if (user?.isPlus() == true) {
            AdaptiveIcons.Outlined.Done
        } else {
            AdaptiveIcons.Outlined.Lock
        }

    var selected by remember {
        mutableStateOf<Subscription?>(null)
    }

    LaunchedEffect(productState) {
        selected = (productState as? PaywallViewModel.State.Data)?.products?.sortedBy { it.first.monthlyPrice.value }?.get(0)?.first
    }

    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(4.dp))
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
                        headlineContent = { Text("Web Billing yet in progress") },
                        supportingContent = { Text("You could subscribe via iOS or Android app!") },
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
                        headlineContent = { Text("Your plus is active") },
                        supportingContent = { Text("Also available in Wesplit mobile ") },
                        leadingContent = {
                            Icon(
                                painter = painterResource(Res.drawable.ic_mobile_app),
                                contentDescription = "Download mobile app",
                            )
                        },
                    )
                }
            }
        }

        selected?.let {
            SubscriptionButton(
                selected = it,
                isPendingPurchase = isPendingPurchase,
            ) {
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
                            painter = painterResource(feature.image),
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
            SubscriptionButton(
                selected = it,
                isPendingPurchase = isPendingPurchase,
            ) {
                onSubscribe(it)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PricingSelection(
    modifier: Modifier = Modifier,
    subscriptions: List<Pair<Subscription, Offer>>,
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
        subscriptions.sortedBy { it.first.monthlyPrice.value }.mapIndexed { index, subscription ->
            if (index == 0) {
                Box {
                    SubscriptionItem(
                        modifier = if (selected == subscription.first) selectedModifier else unSelectedModifier,
                        isSelected = selected == subscription.first,
                        subscription = subscription.first,
                        offer = subscription.second,
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
                    modifier = if (selected == subscription.first) selectedModifier else unSelectedModifier,
                    isSelected = selected == subscription.first,
                    subscription = subscription.first,
                    offer = subscription.second,
                ) {
                    onSelected(it)
                }
            }
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}

@OptIn(ExperimentalAdaptiveApi::class)
@Composable
private fun SubscriptionButton(
    selected: Subscription,
    isPendingPurchase: Boolean,
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
        onClick = { if (!isPendingPurchase) onSelected(selected) },
        shape = RoundedCornerShape(10.dp),
    ) {
        if (isPendingPurchase) {
            AdaptiveCircularProgressIndicator(
                modifier = Modifier.size(30.dp),
                adaptationScope = {
                    cupertino { color = MaterialTheme.extraColorScheme.onInfoContainer }
                    material { color = MaterialTheme.extraColorScheme.onInfoContainer }
                },
            )
        } else {
            Text(
                text = "Subscribe for ${selected.formattedPrice}",
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.7.sp,
                    ),
            )
        }
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
