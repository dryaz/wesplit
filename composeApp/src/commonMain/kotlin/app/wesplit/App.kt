package app.wesplit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import app.wesplit.data.firebase.firebaseDataModule
import app.wesplit.di.appModule
import app.wesplit.domain.di.domainModule
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.LogLevel
import app.wesplit.routing.DeeplinkAction
import app.wesplit.routing.DeeplinkParsers
import app.wesplit.routing.LeftPane
import app.wesplit.routing.MenuItem
import app.wesplit.routing.RightPane
import app.wesplit.routing.RootNavigation
import app.wesplit.theme.AppTheme
import com.motorro.keeplink.uri.data.toMap
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.firestoreSettings
import dev.gitlive.firebase.firestore.persistentCacheSettings
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import org.koin.core.module.Module
import org.koin.dsl.koinApplication

private const val CAMPAIGN_EVENT = "campaign_hit"

@Composable
@Preview
fun App(
    deeplinkUrl: String,
    vararg platformModule: Module,
) {
    Firebase.firestore.settings =
        firestoreSettings {
            persistentCacheSettings { }
        }

    val link = DeeplinkParsers.PROD.parse(deeplinkUrl) ?: DeeplinkParsers.LOCALHOST_8080.parse(deeplinkUrl)

    KoinContext(
        context =
            koinApplication {
                modules(domainModule() + firebaseDataModule() + appModule() + platformModule)
            }.koin,
    ) {
        val analyticsManager: AnalyticsManager = koinInject()

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
                selectedMenuItem = menuItem
            }
        }

        LaunchedEffect(Unit) {
            link?.let { link ->
                if (link.utm.getSearch().isNotEmpty()) {
                    analyticsManager.track(CAMPAIGN_EVENT, link.utm.getSearch().toMap())
                    link.utm.getSearch().forEach {
                        analyticsManager.setParam(it.name, it.value)
                    }
                }
            }

            link?.let {
                when (val action = link.action) {
                    is DeeplinkAction.GroupDetails ->
                        secondPaneNavController.navigate(
                            // TODO: Support token for sharing
                            RightPane.Group.destination(action.groupId),
                            navOptions =
                                navOptions {
                                    launchSingleTop = true
                                    popUpTo(
                                        RightPane.Empty.route,
                                        popUpToBuilder = { inclusive = false },
                                    )
                                },
                        )

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
        }
    }
}
