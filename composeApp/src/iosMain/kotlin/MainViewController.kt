import androidx.compose.ui.window.ComposeUIViewController
import app.wesplit.App
import app.wesplit.domain.model.AnalyticsManager
import org.koin.dsl.module

fun mainViewController() =
    ComposeUIViewController {
        App(
            module { single<AnalyticsManager> { IosAnalyticsManager() } },
        )
    }
