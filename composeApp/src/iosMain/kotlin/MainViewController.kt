import androidx.compose.ui.window.ComposeUIViewController
import app.wesplit.App
import app.wesplit.DeepLinkHandler
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.account.LoginDelegate
import app.wesplit.domain.model.user.ContactListDelegate
import app.wesplit.user.UnsupportedContactListDelegate
import org.koin.dsl.module
import platform.UIKit.UIViewController

fun mainViewController(iosDiHelper: IosDiHelper): UIViewController {
    return ComposeUIViewController {
        App(
            module {
                single<AnalyticsManager> { IosAnalyticsManager() }
                single<DeepLinkHandler> { iosDiHelper.deepLinkHandler }
                single<LoginIosNativeDelegate> { iosDiHelper.loginDelegate }
                single<LoginDelegate> { LoginIosDelegate(get()) }
                // TODO: Support contact list of iOS
                single<ContactListDelegate> { UnsupportedContactListDelegate() }
            },
        )
    }
}
