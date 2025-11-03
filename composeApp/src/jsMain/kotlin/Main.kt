import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import app.wesplit.App
import app.wesplit.DeepLinkHandler
import app.wesplit.DefaultPermissionDelegate
import app.wesplit.DefaultShareDelegate
import app.wesplit.FileDownloadDelegate
import app.wesplit.PermissionsDelegate
import app.wesplit.ShareDelegate
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.AppReviewManager
import app.wesplit.domain.model.NotSupportedAppReviewManager
import app.wesplit.domain.model.ShortcutDelegate
import app.wesplit.domain.model.ShortcutDelegateNotSupport
import app.wesplit.domain.model.account.LoginDelegate
import app.wesplit.domain.model.paywall.BillingDelegate
import app.wesplit.domain.model.paywall.UnsupportedBiilingDelegate
import app.wesplit.domain.model.user.ContactListDelegate
import app.wesplit.user.UnsupportedContactListDelegate
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.jetbrains.skiko.wasm.onWasmReady
import org.koin.dsl.module

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    onWasmReady {
        // TODO: Move settings to config
        // Auto-detect environment: use custom domain for production, Firebase domain for localhost
        val isLocalhost = window.location.hostname == "localhost" || window.location.hostname == "127.0.0.1"
        val authDomain = if (isLocalhost) {
            "wesplit-bill.firebaseapp.com" // Default Firebase domain (allows localhost)
        } else {
            "web.wesplit.app" // Custom production domain
        }
        
        console.log("Environment: ${if (isLocalhost) "Development (localhost)" else "Production"}")
        console.log("Using authDomain: $authDomain")
        
        val options: FirebaseOptions =
            FirebaseOptions(
                applicationId = "1:548791587175:web:4e6228a365ede6fdc05fc2",
                apiKey = "AIzaSyDsaHeM7-_M0utMVZPQNSRsEu5Z5k9BjSw",
                projectId = "wesplit-bill",
                authDomain = authDomain,
                storageBucket = "wesplit-bill.appspot.com",
                gcmSenderId = "548791587175",
                gaTrackingId = "G-5EBQE3J64M",
            )
        Firebase.initialize(null, options)

        // TODO: Parse incoming link as a deeplink
        //  - append UTM to firebase user if any
        //  - map deeplink to destination

        // TODO: Changed destination in app should change browser url
        // TODO: How to support back? Probably propagte it from here

        val deepLinkHandler = DeepLinkHandler()
        deepLinkHandler.handleDeeplink(window.location.toString())

        CanvasBasedWindow("Wesplit") {
            // TODO: Support initial destination
            // TODO: Provide parsed UTM into Common app to have SSOT for utm tracking

            App(
                koinApp = null,
                module {
                    single<CoroutineDispatcher> { Dispatchers.Main }
                    single<LoginDelegate> { LoginJsDelegate() }
                    single<AppReviewManager> { NotSupportedAppReviewManager }
                    single<BillingDelegate> { UnsupportedBiilingDelegate(get()) }
                    single<ShareDelegate> { DefaultShareDelegate }
                    single<AnalyticsManager> { JsAnalyticsManager() }
                    single<PermissionsDelegate> { DefaultPermissionDelegate }
//                    single<AnalyticsManager> { DebugAnalyticsManager() }
                    single<ShortcutDelegate> { ShortcutDelegateNotSupport }
                    single<DeepLinkHandler> { deepLinkHandler }
                    single<FileDownloadDelegate> { JsFileDownloadDelegate() }
                    single<ContactListDelegate> { UnsupportedContactListDelegate() }
                },
            )
        }
    }
}
