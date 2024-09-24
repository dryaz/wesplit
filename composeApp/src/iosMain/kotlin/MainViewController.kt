import androidx.compose.ui.window.ComposeUIViewController
import app.wesplit.App
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.account.LoginDelegate
import app.wesplit.domain.model.user.ContactListDelegate
import app.wesplit.user.UnsupportedContactListDelegate
import org.koin.dsl.module
import platform.UIKit.UIViewController

fun mainViewController(iosDiHelper: IosDiHelper): UIViewController {
    return ComposeUIViewController {
        App(
            // TODO: deeplink from ios, probably via diahelper
            "",
            module {
                single<AnalyticsManager> { IosAnalyticsManager() }
                single<LoginIosNativeDelegate> { iosDiHelper.loginDelegate }
                single<LoginDelegate> { LoginIosDelegate(get()) }
                // TODO: Support contact list of iOS
                single<ContactListDelegate> { UnsupportedContactListDelegate() }
            },
        )
    }
}
