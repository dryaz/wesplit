import app.wesplit.domain.model.paywall.BillingDelegate
import app.wesplit.domain.model.paywall.PurchaseState
import app.wesplit.domain.model.paywall.Subscription

class BillingIosDelegate(
    private val platformDelegate: BillingIosNativeDelegate,
) : BillingDelegate {
    override fun requestPricingUpdate() {
        platformDelegate.requestPricingUpdate()
    }

    override fun subscribe(period: Subscription.Period) {
        platformDelegate.subscribe(period)
    }

    override fun isBillingSupported(): Boolean = platformDelegate.isBillingSupported()

    override fun openPromoRedeem() = platformDelegate.openPromoRedeem()
}

interface BillingIosNativeDelegate : BillingDelegate

class BillingIosRepositoryController(
    private val billingRepository: BillingDelegate.StateRepository,
) {
    fun update(pricingResult: List<Subscription>) {
        billingRepository.update(pricingResult)
    }

    fun onPurchaseSuccess(purchaseId: String) {
        billingRepository.onPurchaseEvent(PurchaseState.Completed(purchaseId))
    }

    fun onPurchaseError() {
        billingRepository.onError()
    }
}
