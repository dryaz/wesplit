import app.wesplit.DeepLinkHandler
import app.wesplit.ShortcutDelegate
import app.wesplit.ShortcutDelegateNotSupport

class IosDiHelper(
    val loginDelegate: LoginIosNativeDelegate,
    val deepLinkHandler: DeepLinkHandler,
    val shortcutDelegate: ShortcutDelegate = ShortcutDelegateNotSupport,
)
