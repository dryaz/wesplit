package app.wesplit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.wesplit.di.AndroidAppModule
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.user.ContactListDelegate
import app.wesplit.user.UnsupportedContactListDelegate
import org.koin.dsl.module
import org.koin.ksp.generated.module

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App(
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
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
