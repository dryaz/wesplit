package app.wesplit.billing

import app.wesplit.domain.model.paywall.Subscription
import org.koin.core.annotation.Single

@Single
class PeriodMapper {
    fun map(period: String): Subscription.Period? =
        when (period) {
            "P1W" -> Subscription.Period.WEEK
            "P1Y" -> Subscription.Period.YEAR
            "P1M" -> Subscription.Period.MONTH
            else -> null
        }

    fun map(period: Subscription.Period): String? =
        when (period) {
            Subscription.Period.YEAR -> "P1Y"
            Subscription.Period.MONTH -> "P1M"
            Subscription.Period.WEEK -> null
        }
}
