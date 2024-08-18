import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import app.wesplit.App
import app.wesplit.LoginDelegate
import app.wesplit.domain.model.AnalyticsManager
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize
import org.jetbrains.skiko.wasm.onWasmReady
import org.koin.dsl.module

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    onWasmReady {
        val options: FirebaseOptions =
            FirebaseOptions(
                applicationId = "1:548791587175:web:4e6228a365ede6fdc05fc2",
                apiKey = "AIzaSyDsaHeM7-_M0utMVZPQNSRsEu5Z5k9BjSw",
                projectId = "wesplit-bill",
                authDomain = "wesplit-bill.firebaseapp.com",
                storageBucket = "wesplit-bill.appspot.com",
                gcmSenderId = "548791587175",
            )
        Firebase.initialize(null, options)

        CanvasBasedWindow("WeSplit") {
            App(
                module {
                    single<LoginDelegate> { LoginJsDelegate() }
                    single<AnalyticsManager> { JsAnalyticsManager() }
                },
            )
        }
    }
}
