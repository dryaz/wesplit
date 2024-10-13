package app.wesplit.paywall

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.account.Account
import app.wesplit.domain.model.account.AccountRepository
import app.wesplit.ui.AdaptiveTopAppBar
import io.github.alexzhirkevich.cupertino.adaptive.icons.AdaptiveIcons
import io.github.alexzhirkevich.cupertino.adaptive.icons.KeyboardArrowLeft
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.back_btn_cd
import split.composeapp.generated.resources.ic_plus

sealed interface PaywallAction {
    data object Back : PaywallAction
}

@Composable
fun PaywallRoute(
    modifier: Modifier = Modifier,
    onAction: (PaywallAction) -> Unit,
) {
    val accountRepository: AccountRepository = koinInject()
    val accountState = accountRepository.get().collectAsState()

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
        )
    }
}

@Composable
fun PaywallScreen(
    modifier: Modifier = Modifier,
    account: Account,
) {
}
