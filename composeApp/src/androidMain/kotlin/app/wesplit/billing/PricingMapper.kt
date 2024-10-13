package app.wesplit.billing

import app.wesplit.domain.model.paywall.Product
import com.android.billingclient.api.ProductDetails
import org.koin.core.annotation.Single

@Single
class PricingMapper(
    private val periodMapper: PeriodMapper,
) {
    fun map(productDetails: ProductDetails): List<Product> {
        return productDetails.subscriptionOfferDetails?.mapNotNull { subs ->
            val pricing = subs.pricingPhases.pricingPhaseList.firstOrNull()
            pricing?.let { price ->
                val period = periodMapper.map(price.billingPeriod)
                period?.let { knownPeriod ->
                    Product.Subscription(
                        period = knownPeriod,
                        title = productDetails.title,
                        description = productDetails.description,
                        amountMicros = price.priceAmountMicros,
                        formattedPrice = price.formattedPrice,
                    )
                }
            }
        } ?: emptyList()
    }
}
