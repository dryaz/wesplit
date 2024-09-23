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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import app.wesplit.di.AndroidAppModule
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.user.ContactListDelegate
import app.wesplit.user.UnsupportedContactListDelegate
import org.koin.dsl.module
import org.koin.ksp.generated.module

class MainActivity : ComponentActivity() {
    // MutableState to hold the deep link URL
    private var deepLinkUrl by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                deeplinkUrl = deepLinkUrl ?: "",
                AndroidAppModule().module,
                module(createdAtStart = true) { single { (application as MainApplication).activityProvider } },
                module {
                    single<AnalyticsManager> { AndroidAnalyticsManager() }
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
        intent?.data?.let { uri ->
            deepLinkUrl = uri.toString()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App("")
}
