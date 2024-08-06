import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.bundle.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import group.detailed.GroupInfoAction
import group.detailed.GroupInfoScreen
import group.detailed.NoGroupScreen
import group.list.GroupItemAction
import group.list.GroupListScreen

sealed class PaneNavigation(val route: String)

sealed class LeftPane(route: String) : PaneNavigation(route) {
    data object GroupList : LeftPane("groups")
}

sealed class RightPane(route: String) : PaneNavigation(route) {
    data object Empty : RightPane("empty")
    data object Group : RightPane("group")
}

@Composable
fun RootNavigation() {
    val firstPaneNavController: NavHostController = rememberNavController()
    val secondPaneNavController: NavHostController = rememberNavController()

    var secondNahControllerEmpty by remember { mutableStateOf(false) }

    LaunchedEffect(secondPaneNavController) {
        secondPaneNavController.addOnDestinationChangedListener(object : NavController.OnDestinationChangedListener {
            override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
                secondNahControllerEmpty = controller.previousBackStackEntry == null
            }
        })
    }

    DoublePaneNavigation(
        secondNavhostEmpty = secondNahControllerEmpty,
        firstNavhost = { modifier ->
            NavHost(
                modifier = modifier,
                navController = firstPaneNavController,
                startDestination = LeftPane.GroupList.route
            ) {
                composable(route = LeftPane.GroupList.route) {
                    GroupListScreen { action ->
                        when (action) {
                            is GroupItemAction.Select -> secondPaneNavController.navigate(RightPane.Group.route)
                        }
                    }
                }
            }
        },
        secondNavhost = { modifier ->
            NavHost(
                modifier = modifier,
                navController = secondPaneNavController,
                startDestination = RightPane.Empty.route
            ) {
                composable(route = RightPane.Empty.route) {
                    NoGroupScreen()
                }
                composable(route = RightPane.Group.route) {
                    GroupInfoScreen { action ->
                        when (action) {
                            GroupInfoAction.Back -> secondPaneNavController.navigateUp()
                        }
                    }
                }
            }
        }
    )
}
