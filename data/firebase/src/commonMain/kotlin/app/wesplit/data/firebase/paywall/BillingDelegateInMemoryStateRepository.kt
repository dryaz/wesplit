package app.wesplit.data.firebase.paywall

import app.wesplit.domain.model.paywall.BillingDelegate
import app.wesplit.domain.model.paywall.BillingState
import app.wesplit.domain.model.paywall.Offer
import app.wesplit.domain.model.paywall.PurchaseState
import app.wesplit.domain.model.paywall.Subscription
import app.wesplit.domain.model.user.Setting
import app.wesplit.domain.model.user.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.annotation.Single
import kotlin.math.roundToInt

@Single
class BillingDelegateInMemoryStateRepository(
    private val userRepository: UserRepository,
) : BillingDelegate.StateRepository {
    private val billingState = MutableStateFlow<BillingState>(BillingState.Loading)

    override fun update(pricingResult: List<Subscription>) {
        val highestMonthlyPrice = pricingResult.maxBy { it.monthlyPrice.value }.monthlyPrice.value
        val offers =
            pricingResult.map { price ->
                price.period to
                    Offer(
                        daysFree =
                            when (price.period) {
                                Subscription.Period.WEEK -> 3
                                Subscription.Period.MONTH -> 7
                                Subscription.Period.YEAR -> 14
                            },
                        discountPercent = ((1.0 - price.monthlyPrice.value / highestMonthlyPrice) * 100.0).roundToInt(),
                    )
            }.toMap()

        billingState.value = BillingState.Data(pricingResult, offers)
    }

    override fun onPurchaseEvent(state: PurchaseState) {
        billingState.value =
            when (state) {
                PurchaseState.Canceled -> BillingState.PurchaseCanceled
                is PurchaseState.Completed -> {
                    state.transactionId?.let {
                        userRepository.update(Setting.TransactionId(it))
                    }
                    BillingState.PurchaseCompleted
                }
            }
    }

    override fun onError() {
        billingState.value = BillingState.Error
    }

    override fun getStream(): Flow<BillingState> = billingState
}
