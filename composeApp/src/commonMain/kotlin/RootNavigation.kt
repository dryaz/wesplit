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
import app.wesplit.domain.model.group.GroupRepository
import group.detailed.GroupInfoAction
import group.detailed.GroupInfoScreen
import group.detailed.GroupInfoViewModel
import group.detailed.NoGroupScreen
import group.list.GroupListAction
import group.list.GroupListRoute
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.compose.koinInject

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
}

@Composable
fun RootNavigation() {
    val firstPaneNavController: NavHostController = rememberNavController()
    val secondPaneNavController: NavHostController = rememberNavController()

    var secondNahControllerEmpty by remember { mutableStateOf(false) }

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
                                // TODO: Implement
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
                    val ioDispatcher: CoroutineDispatcher = koinInject()

                    val viewModel: GroupInfoViewModel =
                        viewModel(
                            key = it.arguments.toString(),
                        ) {
                            GroupInfoViewModel(
                                SavedStateHandle.createHandle(null, it.arguments),
                                groupRepository,
                                ioDispatcher,
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
            }
        },
    )
}
