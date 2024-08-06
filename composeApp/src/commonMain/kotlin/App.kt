import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import app.wesplit.data.firebase.domainModule
import app.wesplit.data.firebase.firebaseDataModule
import di.appModule
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication

@Composable
@Preview
fun App() {
    KoinApplication(application = {
        modules(domainModule() + firebaseDataModule() + appModule())
    }) {
        MaterialTheme {
            RootNavigation()
        }
    }
}
