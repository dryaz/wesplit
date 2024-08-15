import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize
import org.jetbrains.skiko.wasm.onWasmReady

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    onWasmReady {
        val options: FirebaseOptions =
            FirebaseOptions(
                applicationId = "1:548791587175:web:4e6228a365ede6fdc05fc2",
                apiKey = "AIzaSyDsaHeM7-_M0utMVZPQNSRsEu5Z5k9BjSw",
                // TODO: Create/define
                databaseUrl = null,
                projectId = "wesplit-bill",
                authDomain = "wesplit-bill.firebaseapp.com",
                storageBucket = "wesplit-bill.appspot.com",
            )
        Firebase.initialize(null, options)

        CanvasBasedWindow("composeApp") {
            App()
        }
    }
}
