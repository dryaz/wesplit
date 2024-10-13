package app.wesplit.paywall

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.account.Account
import app.wesplit.domain.model.account.AccountRepository
import app.wesplit.domain.model.user.Subscription
import app.wesplit.ui.AdaptiveTopAppBar
import io.github.alexzhirkevich.cupertino.adaptive.icons.AdaptiveIcons
import io.github.alexzhirkevich.cupertino.adaptive.icons.Done
import io.github.alexzhirkevich.cupertino.adaptive.icons.KeyboardArrowLeft
import io.github.alexzhirkevich.cupertino.adaptive.icons.Lock
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.back_btn_cd
import split.composeapp.generated.resources.ic_plus
import split.composeapp.generated.resources.img_feature_protect

sealed interface PaywallAction {
    data object Back : PaywallAction
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

    Scaffold(
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
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PaywallScreen(
    modifier: Modifier = Modifier,
    account: Account,
    productState: PaywallViewModel.State,
) {
    val trailingIcon =
        if ((account as? Account.Authorized)?.user?.subscription == Subscription.BASIC) {
            AdaptiveIcons.Outlined.Lock
        } else {
            AdaptiveIcons.Outlined.Done
        }
    Column(modifier = modifier) {
        Row {
            when (productState) {
                is PaywallViewModel.State.Data ->
                    productState.products.map { product ->
                        Text(
                            modifier = Modifier.padding(32.dp).weight(1f),
                            text = product.toString(),
                        )
                    }

                PaywallViewModel.State.Error -> Text("ERROR IS HERE, GET AWESOME HANDLER")
                PaywallViewModel.State.Loading -> CircularProgressIndicator() // TODO: Three items to load
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(1f).padding(vertical = 16.dp).verticalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            features.map { feature ->
                Box(modifier = Modifier.padding(horizontal = 8.dp)) {
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
    }
}
