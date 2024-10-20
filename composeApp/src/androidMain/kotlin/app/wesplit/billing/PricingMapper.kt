package app.wesplit.billing

import app.wesplit.domain.model.currency.Amount
import app.wesplit.domain.model.paywall.Subscription
import com.android.billingclient.api.ProductDetails
import org.koin.core.annotation.Single

private const val MICROS_DEL = 1000000

@Single
class PricingMapper(
    private val periodMapper: PeriodMapper,
) {
    fun map(productDetails: ProductDetails): List<Subscription> {
        return productDetails.subscriptionOfferDetails?.mapNotNull { subs ->
            val pricing = subs.pricingPhases.pricingPhaseList.firstOrNull()
            pricing?.let { price ->
                val period = periodMapper.map(price.billingPeriod)
                period?.let { knownPeriod ->
                    Subscription(
                        period = knownPeriod,
                        title = productDetails.title,
                        description = productDetails.description,
                        monthlyPrice =
                            Amount(
                                value =
                                    when (knownPeriod) {
                                        Subscription.Period.WEEK -> price.priceAmountMicros.toDouble() / MICROS_DEL * 4
                                        Subscription.Period.MONTH -> price.priceAmountMicros.toDouble() / MICROS_DEL
                                        Subscription.Period.YEAR -> price.priceAmountMicros.toDouble() / MICROS_DEL / 12
                                    },
                                currencyCode = price.priceCurrencyCode,
                            ),
                        formattedPrice = price.formattedPrice,
                    )
                }
            }
        } ?: emptyList()
    }
}
