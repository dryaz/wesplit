import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.bundle.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.wesplit.data.firebase.domainModule
import app.wesplit.data.firebase.firebaseDataModule
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication

enum class LeftPaneScreens(val title: String) {
    First("First"),
    Second("Second"),
    Third("Third"),
    Fourth("Fourth"),
    Fifth("Fifth"),
    Six("Six")
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
@Preview
fun App() {
    KoinApplication(application = {
        modules(domainModule() + firebaseDataModule())
    }) {
        MaterialTheme {
            val firstPaneNavController: NavHostController = rememberNavController()
            val secondPaneNavController: NavHostController = rememberNavController()

            var secondNahControllerEmpty by remember { mutableStateOf(true) }

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
                        modifier = modifier.verticalScroll(rememberScrollState()),
                        navController = firstPaneNavController,
                        startDestination = LeftPaneScreens.First.name
                    ) {
                        composable(route = LeftPaneScreens.First.name) {
                            Screen("Screen 1") { firstPaneNavController.navigate(LeftPaneScreens.Second.name) }
                        }
                        composable(route = LeftPaneScreens.Second.name) {
                            Screen("Screen 2") {
                                firstPaneNavController.navigate(LeftPaneScreens.Third.name)
                                secondPaneNavController.navigate(LeftPaneScreens.Fifth.name)
                            }
                        }
                        composable(route = LeftPaneScreens.Third.name) {
                            Screen("Screen 3") { firstPaneNavController.navigateUp() }
                        }
                    }
                },
                secondNavhost = { modifier ->
                    NavHost(
                        modifier = modifier,
                        navController = secondPaneNavController,
                        startDestination = LeftPaneScreens.Fourth.name
                    ) {
                        composable(route = LeftPaneScreens.Fourth.name) {
                            RichScreen("Screen 4") { secondPaneNavController.navigate(LeftPaneScreens.Fifth.name) }
                        }
                        composable(route = LeftPaneScreens.Fifth.name) {
                            RichScreen("Screen 5") { secondPaneNavController.navigateUp() }
                        }
                        composable(route = LeftPaneScreens.Six.name) {
                            RichScreen("Screen 6") { secondPaneNavController.navigateUp() }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun Screen(title: String, onClick: () -> Unit) {
    Text(
        modifier = Modifier.fillMaxSize(1f).background(Color.Cyan).clickable {
            onClick()
        },
        text = title
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RichScreen(title: String, onClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(title)
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onClick() }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        Text("Rich screen: $title")
    }
}
