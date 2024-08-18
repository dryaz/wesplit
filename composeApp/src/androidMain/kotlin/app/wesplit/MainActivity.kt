package app.wesplit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.wesplit.di.AndroidAppModule
import app.wesplit.domain.model.AnalyticsManager
import org.koin.dsl.module
import org.koin.ksp.generated.module

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App(
                AndroidAppModule().module,
                module(createdAtStart = true) { single { (application as MainApplication).activityProvider } },
                module { single<AnalyticsManager> { AndroidAnalyticsManager() } },
            )
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
