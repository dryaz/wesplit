import app.wesplit.DeepLinkHandler
import app.wesplit.ShareDelegate
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin

class IosDiHelper(
    val loginDelegate: LoginIosNativeDelegate,
    val deepLinkHandler: DeepLinkHandler,
    val shareDelegate: ShareDelegate,
    val billingDelegate: BillingIosNativeDelegate,
) : KoinComponent {
    val koinApp: KoinApplication

    init {
        koinApp = startKoin {}
    }

    fun diHolder(): DiHolder = get()
}
