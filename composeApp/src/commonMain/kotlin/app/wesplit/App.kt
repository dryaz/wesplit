package app.wesplit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import app.wesplit.data.firebase.firebaseDataModule
import app.wesplit.domain.di.domainModule
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.LogLevel
import app.wesplit.domain.model.experiment.ExperimentRepository
import app.wesplit.domain.model.feature.FeatureRepository
import app.wesplit.routing.DeeplinkAction
import app.wesplit.routing.DeeplinkParsers
import app.wesplit.routing.LeftPane
import app.wesplit.routing.MenuItem
import app.wesplit.routing.RightPane
import app.wesplit.routing.RootNavigation
import app.wesplit.theme.AppTheme
import com.motorro.keeplink.uri.data.getValue
import com.motorro.keeplink.uri.data.toMap
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.koinApplication

private const val CAMPAIGN_EVENT = "campaign_hit"

private const val DEEPLINK_EVENT = "deeplink_open"
private const val DEEPLINK_URL_PARAM = "url"

@Composable
@Preview
fun App(
    koinApp: KoinApplication? = null,
    vararg platformModule: Module,
) {
    KoinContext(
        context =
            (
                koinApp?.modules(domainModule() + firebaseDataModule() + platformModule) ?: koinApplication {
                    modules(domainModule() + firebaseDataModule() + platformModule)
                }
            ).koin,
    ) {
        val deepLinkHandler: DeepLinkHandler = koinInject()
        val analyticsManager: AnalyticsManager = koinInject()
        val featureRepository: FeatureRepository = koinInject()
        val experimentRepository: ExperimentRepository = koinInject()

        val deeplink = deepLinkHandler.deeplink.collectAsState()

        val firstPaneNavController: NavHostController = rememberNavController()
        val secondPaneNavController: NavHostController = rememberNavController()

        var selectedMenuItem: NavigationMenuItem by remember {
            mutableStateOf(MenuItem.Group)
        }

        AppTheme {
            RootNavigation(
                firstPaneNavController = firstPaneNavController,
                secondPaneNavController = secondPaneNavController,
                selectedMenuItem = selectedMenuItem,
            ) { menuItem ->
                if ((menuItem as? NavigationMenuItem.Item)?.selectable == true) {
                    selectedMenuItem = menuItem
                }
            }
        }

        LaunchedEffect(Unit) {
            featureRepository.refresh()
            experimentRepository.refresh()
        }

        LaunchedEffect(deeplink.value) {
            val linkValue = deeplink.value

            val link = DeeplinkParsers.PROD.parse(linkValue) ?: DeeplinkParsers.LOCALHOST_8080.parse(linkValue)
            link?.let { link ->
                if (link.utm.getSearch().isNotEmpty()) {
                    analyticsManager.track(CAMPAIGN_EVENT, link.utm.getSearch().toMap())
                    link.utm.getSearch().forEach {
                        analyticsManager.setParam(it.name, it.value)
                    }
                }
            }

            link?.let {
                analyticsManager.track(
                    DEEPLINK_EVENT,
                    mapOf(
                        DEEPLINK_URL_PARAM to linkValue,
                    ),
                )

                when (val action = link.action) {
                    is DeeplinkAction.Group.Details -> {
                        secondPaneNavController.navigate(
                            RightPane.Group.destination(
                                groupId = action.groupId,
                                token = link.action.getSearch().getValue(DeeplinkAction.Group.Details.TOKEN),
                            ),
                            navOptions =
                                navOptions {
                                    launchSingleTop = true
                                    popUpTo(
                                        RightPane.Empty.route,
                                        popUpToBuilder = { inclusive = false },
                                    )
                                },
                        )
                    }

                    is DeeplinkAction.Group.Expense -> {
                        secondPaneNavController.navigate(
                            RightPane.ExpenseDetails.destination(action.groupId),
                            navOptions =
                                navOptions {
                                    launchSingleTop = true
                                    popUpTo(
                                        RightPane.Empty.route,
                                        popUpToBuilder = { inclusive = false },
                                    )
                                },
                        )
                    }

                    is DeeplinkAction.Home -> {}
                    is DeeplinkAction.Profile ->
                        firstPaneNavController.navigate(
                            LeftPane.Profile.route,
                            navOptions =
                                navOptions {
                                    launchSingleTop = true
                                    popUpTo(
                                        LeftPane.GroupList.route,
                                        popUpToBuilder = { inclusive = false },
                                    )
                                },
                        )

                    else -> analyticsManager.log("Unexpected navigation to $link", LogLevel.WARNING)
                }
            }

            deepLinkHandler.consume()
        }
    }
}
