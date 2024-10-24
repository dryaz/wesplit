package app.wesplit.domain.model.paywall

import app.wesplit.domain.model.currency.Amount
import kotlinx.coroutines.flow.Flow

interface BillingDelegate {
    fun requestPricingUpdate()

    fun subscribe(period: Subscription.Period)

    fun isBillingSupported(): Boolean

    fun openPromoRedeem()

    interface StateRepository {
        fun update(pricingResult: List<Subscription>)

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

    data class Data(val data: List<Subscription>, val offer: Map<Subscription.Period, Offer>) : BillingState()
}

sealed interface PurchaseState {
    data object Canceled : PurchaseState

    data class Completed(val transactionId: String? = null) : PurchaseState
}

class UnsupportedBiilingDelegate(val repository: BillingDelegate.StateRepository) : BillingDelegate {
    override fun requestPricingUpdate() {
        val products =
            listOf<Subscription>(
                Subscription(
                    title = "Monthly subscription",
                    description = "Get full from your wesplit with montly",
                    formattedPrice = "$1.99",
                    monthlyPrice = Amount(1.99, "USD"),
                    period = Subscription.Period.MONTH,
                ),
                Subscription(
                    title = "Weekly subscription",
                    description = "Get full from your wesplit with montly",
                    formattedPrice = "$0.99",
                    monthlyPrice = Amount(2.39, "USD"),
                    period = Subscription.Period.WEEK,
                ),
                Subscription(
                    title = "Yearly subscription",
                    description = "Get full from your wesplit with montly",
                    formattedPrice = "$19.99",
                    monthlyPrice = Amount(1.39, "USD"),
                    period = Subscription.Period.YEAR,
                ),
            )
        repository.update(products)
    }

    override fun subscribe(period: Subscription.Period) {
        TODO("Not yet implemented")
    }

    override fun isBillingSupported(): Boolean = false

    override fun openPromoRedeem() {
        TODO("Not yet implemented")
    }
}
