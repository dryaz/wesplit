package app.wesplit.domain.model.paywall

import app.wesplit.domain.model.currency.Amount
import kotlinx.coroutines.flow.Flow

interface BillingDelegate {
    fun requestPricingUpdate()

    fun subscribe(period: Subscription.Period)

    fun isBillingSupported(): Boolean

    interface StateRepository {
        fun update(pricingResult: Result<List<Subscription>>)

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

    data class Data(val data: List<Subscription>) : BillingState()
}

enum class PurchaseState {
    COMPLETED,
    CANCELED,
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
        repository.update(Result.success(products))
    }

    override fun subscribe(period: Subscription.Period) {
        TODO("Not yet implemented")
    }

    override fun isBillingSupported(): Boolean = false
}
