package app.wesplit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import app.wesplit.data.firebase.firebaseDataModule
import app.wesplit.di.appModule
import app.wesplit.domain.di.domainModule
import app.wesplit.routing.MenuItem
import app.wesplit.routing.RootNavigation
import app.wesplit.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.koin.core.module.Module
import org.koin.dsl.koinApplication

@Composable
@Preview
fun App(vararg platformModule: Module) {
    KoinContext(context = koinApplication {
        modules(domainModule() + firebaseDataModule() + appModule() + platformModule)
    }.koin) {
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
    }
}
