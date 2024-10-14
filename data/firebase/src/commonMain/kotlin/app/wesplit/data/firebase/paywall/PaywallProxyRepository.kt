package app.wesplit.data.firebase.paywall

import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.paywall.BillingDelegate
import app.wesplit.domain.model.paywall.BillingState
import app.wesplit.domain.model.paywall.PaywallRepository
import app.wesplit.domain.model.paywall.PaywallRestrictionException
import app.wesplit.domain.model.paywall.Subscription
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Single

private const val PURCHASE_PERIOD_PARAM = "period"
private const val PURCHASE_FAIL_REASON_PARAM = "reason"

private const val PURCHASE_ATTEMPT_EVENT = "purchase_attempt"
private const val PURCHASE_COMPLETED_EVENT = "purchase_completed"
private const val PURCHASE_FAILED_EVENT = "purchase_failed"

@Single
class PaywallProxyRepository(
    private val billingDelegate: BillingDelegate,
    private val billingStateRepository: BillingDelegate.StateRepository,
    private val analytics: AnalyticsManager,
) : PaywallRepository {
    override suspend fun getProducts(): Result<List<Subscription>> {
        // TODO: If it's already purchased - return exception?
        billingDelegate.requestPricingUpdate()

        val result =
            billingStateRepository.getStream().filter { billingState ->
                billingState is BillingState.Data || billingState is BillingState.Error
            }.first()

        return when (result) {
            is BillingState.Data -> Result.success(result.data)
            BillingState.Error -> Result.failure(PaywallRestrictionException("Can't get PRO prices"))
            else -> throw IllegalStateException("Data or Error states only should be pro pricing call")
        }
    }

    override suspend fun subscribe(period: Subscription.Period): Result<Boolean> {
        analytics.track(PURCHASE_ATTEMPT_EVENT, mapOf(PURCHASE_PERIOD_PARAM to period.name))

        // TODO: If it's already purchased - return exception?
        billingDelegate.subscribe(period)
        val result =
            billingStateRepository.getStream().filter { billingState ->
                billingState is BillingState.PurchaseCompleted ||
                    billingState is BillingState.PurchaseCanceled ||
                    billingState is BillingState.Error
            }.first()

        return when (result) {
            BillingState.PurchaseCompleted -> {
                analytics.track(PURCHASE_COMPLETED_EVENT, mapOf(PURCHASE_PERIOD_PARAM to period.name))
                Result.success(true)
            }

            BillingState.PurchaseCanceled -> {
                analytics.track(
                    PURCHASE_FAILED_EVENT,
                    mapOf(PURCHASE_PERIOD_PARAM to period.name, PURCHASE_FAIL_REASON_PARAM to "canceled"),
                )
                Result.success(false)
            }

            BillingState.Error -> {
                analytics.track(
                    PURCHASE_FAILED_EVENT,
                    mapOf(PURCHASE_PERIOD_PARAM to period.name, PURCHASE_FAIL_REASON_PARAM to "error"),
                )
                Result.failure(PaywallRestrictionException("Can't proceed payment"))
            }

            else -> throw IllegalStateException("Purchases or Error states only should be pro purchase call")
        }
    }

    override fun isBillingSupported(): Boolean = billingDelegate.isBillingSupported()
}
