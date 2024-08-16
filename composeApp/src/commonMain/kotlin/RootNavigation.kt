import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.bundle.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.account.AccountRepository
import app.wesplit.domain.model.group.GroupRepository
import dev.gitlive.firebase.auth.FirebaseUser
import group.detailed.GroupInfoAction
import group.detailed.GroupInfoScreen
import group.detailed.GroupInfoViewModel
import group.detailed.NoGroupScreen
import group.list.GroupListAction
import group.list.GroupListRoute
import group.settings.GroupSettingsAction
import group.settings.GroupSettingsScreen
import group.settings.GroupSettingsViewModel
import org.koin.compose.koinInject

private const val LOGIN_ATTEMPT_EVENT = "login_attempt"
private const val LOGIN_SUCCEED_EVENT = "login_succeed"
private const val LOGIN_FAILED_EVENT = "login_failed"

private const val LOGIN_PROVIDER_PARAM = "provider"

sealed class PaneNavigation(
    val route: String,
) {
    open fun destination(): String = route
}

sealed class LeftPane(
    route: String,
) : PaneNavigation(route) {
    data object GroupList : LeftPane("groups")
}

sealed class RightPane(
    route: String,
) : PaneNavigation(route) {
    data object Empty : RightPane("empty")

    data object Group : RightPane("group/{${Param.GROUP_ID.paramName}}") {
        enum class Param(
            val paramName: String,
        ) {
            GROUP_ID("group_id"),
        }

        fun destination(groupId: String): String = "group/$groupId"

        override fun destination(): String = throw IllegalArgumentException("Must use destination(groupId: String) instead")
    }

    data object NewGroup : RightPane("newGroup")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootNavigation() {
    val firstPaneNavController: NavHostController = rememberNavController()
    val secondPaneNavController: NavHostController = rememberNavController()

    var secondNahControllerEmpty by remember { mutableStateOf(false) }
    val analytics: AnalyticsManager = koinInject()
    val accountRepository: AccountRepository = koinInject()

    LaunchedEffect(secondPaneNavController) {
        secondPaneNavController.addOnDestinationChangedListener(
            object : NavController.OnDestinationChangedListener {
                override fun onDestinationChanged(
                    controller: NavController,
                    destination: NavDestination,
                    arguments: Bundle?,
                ) {
                    secondNahControllerEmpty = controller.previousBackStackEntry == null
                }
            },
        )
    }

    DoublePaneNavigation(
        secondNavhostEmpty = secondNahControllerEmpty,
        firstNavhost = { modifier ->
            NavHost(
                modifier = modifier,
                navController = firstPaneNavController,
                startDestination = LeftPane.GroupList.route,
            ) {
                composable(route = LeftPane.GroupList.route) {
                    GroupListRoute { action ->
                        when (action) {
                            is GroupListAction.Select ->
                                secondPaneNavController.navigate(
                                    RightPane.Group.destination(action.group.id),
                                    navOptions =
                                        navOptions {
                                            launchSingleTop = true
                                        },
                                )

                            GroupListAction.Login -> {
                                // TODO: Proper login via firebase and check who and how should notify repo
                                // accountRepository.login()
                                val loginType = LoginType.GOOGLE
                                val providerParam = mapOf(LOGIN_PROVIDER_PARAM to loginType.toString())
                                analytics.track(LOGIN_ATTEMPT_EVENT, providerParam)
                                login(LoginType.GOOGLE) { result ->
                                    if (result.isSuccess) {
                                        analytics.track(LOGIN_SUCCEED_EVENT, providerParam)
                                        println(result.getOrThrow().email)
                                        println(result.getOrThrow().photoURL)
                                        // TODO: Login via repo
                                    } else {
                                        analytics.track(LOGIN_FAILED_EVENT, providerParam)
                                        result.exceptionOrNull()?.let {
                                            analytics.log(it)
                                        }
                                    }
                                }
                            }

                            GroupListAction.CreateNewGroup -> {
                                secondPaneNavController.navigate(
                                    RightPane.NewGroup.destination(),
                                    navOptions =
                                        navOptions {
                                            launchSingleTop = true
                                        },
                                )
                            }
                        }
                    }
                }
            }
        },
        secondNavhost = { modifier ->
            NavHost(
                modifier = modifier,
                navController = secondPaneNavController,
                startDestination = RightPane.Empty.route,
            ) {
                composable(route = RightPane.Empty.route) {
                    NoGroupScreen()
                }
                composable(
                    route = RightPane.Group.route,
                    arguments =
                        listOf(
                            navArgument(RightPane.Group.Param.GROUP_ID.paramName) {
                                type = NavType.StringType
                            },
                        ),
                ) {
                    val groupRepository: GroupRepository = koinInject()

                    val viewModel: GroupInfoViewModel =
                        viewModel(
                            key = it.arguments.toString(),
                        ) {
                            GroupInfoViewModel(
                                SavedStateHandle.createHandle(null, it.arguments),
                                groupRepository,
                            )
                        }

                    GroupInfoScreen(
                        viewModel = viewModel,
                    ) { action ->
                        when (action) {
                            GroupInfoAction.Back -> secondPaneNavController.navigateUp()
                        }
                    }
                }
                composable(route = RightPane.NewGroup.route) {
                    val groupRepository: GroupRepository = koinInject()

                    val viewModel: GroupSettingsViewModel =
                        viewModel {
                            GroupSettingsViewModel(
                                SavedStateHandle.createHandle(null, null),
                                groupRepository,
                            )
                        }

                    GroupSettingsScreen(viewModel = viewModel) { action ->
                        when (action) {
                            GroupSettingsAction.Back -> secondPaneNavController.navigateUp()
                        }
                    }
                }
            }
        },
    )
}

expect fun login(
    type: LoginType,
    onLogin: (Result<FirebaseUser>) -> Unit,
)

enum class LoginType {
    GOOGLE,
}
