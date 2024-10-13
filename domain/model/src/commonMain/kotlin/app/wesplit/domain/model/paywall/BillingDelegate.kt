package app.wesplit.domain.model.paywall

import kotlinx.coroutines.flow.Flow

interface BillingDelegate {
    fun requestPricingUpdate()

    fun subscribe(period: Product.Subscription.Period)

    interface StateRepository {
        fun update(pricingResult: Result<List<Product>>)

        fun getStream(): Flow<BillingState>

        fun onPurchaseEvent(state: PurchaseState)

        fun onError()
    }
}

sealed class BillingState {
    data object Loading : BillingState()

    data object Error : BillingState()

    data object PurchaseCompleted : BillingState()

    data object PurchaseCanceled : BillingState()

    data class Data(val data: List<Product>) : BillingState()
}

enum class PurchaseState {
    COMPLETED,
    CANCELED,
}

class UnsupportedBiilingDelegate(val repository: BillingDelegate.StateRepository) : BillingDelegate {
    override fun requestPricingUpdate() {
        repository.onError()
    }

    override fun subscribe(period: Product.Subscription.Period) {
        TODO("Not yet implemented")
    }
}
