import androidx.compose.runtime.Composable
import app.wesplit.data.firebase.domainModule
import app.wesplit.data.firebase.firebaseDataModule
import di.appModule
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.core.module.Module
import theme.AppTheme

@Composable
@Preview
fun App(platformModule: Module? = null) {
    KoinApplication(application = {
        if (platformModule != null) {
            modules(domainModule() + firebaseDataModule() + appModule() + platformModule)
        } else {
            modules(domainModule() + firebaseDataModule() + appModule())
        }
    }) {
        AppTheme {
            RootNavigation()
        }
    }
}
