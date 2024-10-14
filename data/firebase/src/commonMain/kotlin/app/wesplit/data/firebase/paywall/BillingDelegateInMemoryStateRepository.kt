package app.wesplit.data.firebase.paywall

import app.wesplit.domain.model.paywall.BillingDelegate
import app.wesplit.domain.model.paywall.BillingState
import app.wesplit.domain.model.paywall.PurchaseState
import app.wesplit.domain.model.paywall.Subscription
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.annotation.Single

@Single
class BillingDelegateInMemoryStateRepository : BillingDelegate.StateRepository {
    private val billingState = MutableStateFlow<BillingState>(BillingState.Loading)

    override fun update(pricingResult: Result<List<Subscription>>) {
        if (pricingResult.isSuccess) {
            billingState.value = BillingState.Data(pricingResult.getOrThrow())
        } else {
            billingState.value = BillingState.Error
        }
    }

    override fun onPurchaseEvent(state: PurchaseState) {
        billingState.value =
            when (state) {
                PurchaseState.COMPLETED -> BillingState.PurchaseCompleted
                PurchaseState.CANCELED -> BillingState.PurchaseCanceled
            }
    }

    override fun onError() {
        billingState.value = BillingState.Error
    }

    override fun getStream(): Flow<BillingState> = billingState
}
