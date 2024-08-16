package app.wesplit

import androidx.compose.runtime.Composable
import app.wesplit.data.firebase.domainModule
import app.wesplit.data.firebase.firebaseDataModule
import app.wesplit.di.appModule
import app.wesplit.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.core.module.Module

@Composable
@Preview
fun App(vararg platformModule: Module) {
    KoinApplication(application = {
        modules(domainModule() + firebaseDataModule() + appModule() + platformModule)
    }) {
        AppTheme {
            RootNavigation()
        }
    }
}
