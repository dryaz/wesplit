import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import app.wesplit.data.firebase.domainModule
import app.wesplit.data.firebase.firebaseDataModule
import kotlinx.browser.document
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    startKoin {
        modules(
            domainModule() + firebaseDataModule()
        )
    }

    ComposeViewport(document.body!!) {
        App()
    }
}