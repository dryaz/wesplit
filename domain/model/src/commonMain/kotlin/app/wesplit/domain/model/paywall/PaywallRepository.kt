package app.wesplit.domain.model.paywall

import app.wesplit.domain.model.currency.Amount

interface PaywallRepository {
    suspend fun getProducts(): Result<List<Subscription>>

    suspend fun subscribe(period: Subscription.Period): Result<Boolean>

    fun isBillingSupported(): Boolean
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
