package app.wesplit.billing

import app.wesplit.domain.model.paywall.Product
import org.koin.core.annotation.Single

@Single
class PeriodMapper {
    fun map(period: String): Product.Subscription.Period? =
        when (period) {
            "P1W" -> Product.Subscription.Period.WEEK
            "P1Y" -> Product.Subscription.Period.YEAR
            "P1M" -> Product.Subscription.Period.MONTH
            else -> null
        }

    fun map(period: Product.Subscription.Period): String? =
        when (period) {
            Product.Subscription.Period.YEAR -> "P1Y"
            Product.Subscription.Period.MONTH -> "P1M"
            Product.Subscription.Period.WEEK -> null
        }
}
