import androidx.compose.ui.window.ComposeUIViewController
import app.wesplit.App
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.account.LoginDelegate
import org.koin.dsl.module
import platform.UIKit.UIViewController

fun mainViewController(): UIViewController {
    return ComposeUIViewController {
        App(
            module {
                single<AnalyticsManager> { IosAnalyticsManager() }
                single<LoginDelegate> { LoginIosDelegate() }
            },
        )
    }
}
