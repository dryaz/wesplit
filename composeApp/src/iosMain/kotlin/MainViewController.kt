import androidx.compose.ui.window.ComposeUIViewController
import app.IosPermissionDelegate
import app.wesplit.App
import app.wesplit.DeepLinkHandler
import app.wesplit.PermissionsDelegate
import app.wesplit.ShareDelegate
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.AppReviewManager
import app.wesplit.domain.model.ShortcutDelegate
import app.wesplit.domain.model.account.LoginDelegate
import app.wesplit.domain.model.paywall.BillingDelegate
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
            koinApp = iosDiHelper.koinApp,
            module {
                single<CoroutineDispatcher> { Dispatchers.IO }
                single<AnalyticsManager> { IosAnalyticsManager() }
                single<AppReviewManager> { IosAppReviewManager() }
                single<BillingIosRepositoryController> { BillingIosRepositoryController(get()) }
                single<IosDiHelper> { iosDiHelper }
                single<PermissionsDelegate> { IosPermissionDelegate }
                single<ShareDelegate> { iosDiHelper.shareDelegate }
                single<ShortcutDelegate> { ShortcutIosDelegate() }
                single<DeepLinkHandler> { iosDiHelper.deepLinkHandler }
                single<BillingIosNativeDelegate> { iosDiHelper.billingDelegate }
                single<BillingDelegate> { BillingIosDelegate(get()) }
                single<LoginIosNativeDelegate> { iosDiHelper.loginDelegate }
                single<LoginDelegate> { LoginIosDelegate(get()) }
                // TODO: Support contact list of iOS
                single<ContactListDelegate> { UnsupportedContactListDelegate() }
                single<DiHolder> { DiHolder() }
            },
        )
    }
}
