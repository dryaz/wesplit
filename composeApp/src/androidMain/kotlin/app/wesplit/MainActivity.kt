package app.wesplit

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.wesplit.billing.GooglePlayBillingDelegate
import app.wesplit.di.AndroidAppModule
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.AppReviewManager
import app.wesplit.domain.model.paywall.BillingDelegate
import app.wesplit.domain.model.user.ContactListDelegate
import app.wesplit.user.UnsupportedContactListDelegate
import io.github.vinceglb.filekit.core.FileKit
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module
import org.koin.ksp.generated.module

class MainActivity : ComponentActivity() {
    private val deepLinkHandler = DeepLinkHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FileKit.init(this)
        handleDeepLinkIntent(intent)
        enableEdgeToEdge(
            statusBarStyle =
                SystemBarStyle.light(
                    scrim = Color.TRANSPARENT,
                    darkScrim = Color.TRANSPARENT,
                ),
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = true
        }
        setContent {
            App(
                koinApp = null,
                AndroidAppModule().module,
                module(createdAtStart = true) { single { (application as MainApplication).activityProvider } },
                module {
                    single<BillingDelegate> {
                        GooglePlayBillingDelegate(
                            activityProvider = get(),
                            context = application,
                            billingStateRepository = get(),
                            pricingMapper = get(),
                            periodMapper = get(),
                            userRepository = get(),
                            analytics = get(),
                        )
                    }
                    single<ShortcutDelegate> { ShortcutAndroidDelegate(application) }
                    single<CoroutineDispatcher> { Dispatchers.IO }
                    single<AppReviewManager> { AndroidAppReviewManager(get()) }
                    single<AnalyticsManager> { AndroidAnalyticsManager() }
//                    single<AnalyticsManager> { DebugAnalyticsManager() }
                    single<ShareDelegate> { AndroidShareDelegate(get(), get()) }
                    single<DeepLinkHandler> { deepLinkHandler }
                    // TODO: Support user's contacts
                    single<ContactListDelegate> { UnsupportedContactListDelegate() }
                },
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLinkIntent(intent)
    }

    private fun handleDeepLinkIntent(intent: Intent?) {
        intent?.let {
            val url = ShortcutAndroidDelegate.getDeeplink(it) ?: it.data?.let { it.toString() }
            url?.let {
                deepLinkHandler.handleDeeplink(it)
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
