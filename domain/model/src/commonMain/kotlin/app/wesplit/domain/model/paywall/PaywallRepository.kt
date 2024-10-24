package app.wesplit.domain.model.paywall

import app.wesplit.domain.model.currency.Amount

interface PaywallRepository {
    suspend fun getProducts(): Result<List<Pair<Subscription, Offer>>>

    suspend fun subscribe(period: Subscription.Period): Result<Boolean>

    fun isBillingSupported(): Boolean

    fun openPromoRedeem()
}

data class Subscription(
    val title: String,
    val description: String,
    val formattedPrice: String,
    val monthlyPrice: Amount,
    val period: Period,
) {
    enum class Period {
        WEEK,
        MONTH,
        YEAR,
    }
}

data class Offer(
    val daysFree: Int,
    val discountPercent: Int,
)
