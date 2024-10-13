package app.wesplit.domain.model.paywall

interface PaywallRepository {
    suspend fun getProducts(): Result<List<Product>>

    suspend fun subscribe(period: Product.Subscription.Period): Result<Boolean>
}

sealed interface Product {
    data class Subscription(
        val title: String,
        val description: String,
        val formattedPrice: String,
        val amountMicros: Long,
        val period: Period,
    ) : Product {
        enum class Period {
            WEEK,
            MONTH,
            YEAR,
        }
    }
}
