import androidx.compose.ui.window.ComposeUIViewController
import app.wesplit.App
import app.wesplit.DeepLinkHandler
import app.wesplit.ShareDelegate
import app.wesplit.ShortcutDelegate
import app.wesplit.ShortcutDelegateNotSupport
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.AppReviewManager
import app.wesplit.domain.model.account.LoginDelegate
import app.wesplit.domain.model.user.ContactListDelegate
import app.wesplit.user.UnsupportedContactListDelegate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.dsl.module
import platform.UIKit.UIViewController

fun mainViewController(iosDiHelper: IosDiHelper): UIViewController {
    return ComposeUIViewController {
        App(
            module {
                single<CoroutineDispatcher> { Dispatchers.IO }
                single<AnalyticsManager> { IosAnalyticsManager() }
                single<AppReviewManager> { IosAppReviewManager() }
                single<ShareDelegate> { iosDiHelper.shareDelegate }
                single<DeepLinkHandler> { iosDiHelper.deepLinkHandler }
                single<ShortcutDelegate> { ShortcutDelegateNotSupport }
                single<LoginIosNativeDelegate> { iosDiHelper.loginDelegate }
                single<LoginDelegate> { LoginIosDelegate(get()) }
                // TODO: Support contact list of iOS
                single<ContactListDelegate> { UnsupportedContactListDelegate() }
            },
        )
    }
}
