package app.wesplit.billing

import android.content.Context
import app.wesplit.di.ActivityProvider
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.LogLevel
import app.wesplit.domain.model.paywall.BillingDelegate
import app.wesplit.domain.model.paywall.PaywallRestrictionException
import app.wesplit.domain.model.paywall.PurchaseState
import app.wesplit.domain.model.paywall.Subscription
import app.wesplit.domain.model.user.UserRepository
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.queryProductDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "GooglePlayBillingDelegate"

private const val PLUS_SUB_ID = "plus"
private const val DELAY_THRESHOLD = 2000L

@Single
class GooglePlayBillingDelegate(
    private val activityProvider: ActivityProvider,
    private val context: Context,
    private val billingStateRepository: BillingDelegate.StateRepository,
    private val pricingMapper: PricingMapper,
    private val periodMapper: PeriodMapper,
    private val userRepository: UserRepository,
    private val analytics: AnalyticsManager,
) : BillingDelegate {
    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            analytics.log("$billingResult", LogLevel.DEBUG)
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                billingStateRepository.onPurchaseEvent(PurchaseState.Completed())
            } else {
                billingStateRepository.onPurchaseEvent(PurchaseState.Canceled)
            }
        }

    private val bgScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var billingClient =
        lazy {
            BillingClient.newBuilder(context)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
                .build()
        }

    private val connectionState = AtomicBoolean(false)

    private var productDetails: ProductDetails? = null

    override fun requestPricingUpdate() {
        bgScope.launch {
            ensureConnection()

            val productList = mutableListOf<QueryProductDetailsParams.Product>()
            productList.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(PLUS_SUB_ID)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build(),
            )

            val params = QueryProductDetailsParams.newBuilder()
            params.setProductList(productList)

            val result = billingClient.value.queryProductDetails(params.build())
            analytics.log(result.billingResult.debugMessage, LogLevel.DEBUG, TAG)
            analytics.log(
                "Response code: ${result.billingResult.responseCode}",
                LogLevel.DEBUG,
                TAG,
            )
            analytics.log("Product details list: ${result.productDetailsList}", LogLevel.DEBUG, TAG)
            if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK || result.productDetailsList.isNullOrEmpty()) {
                billingStateRepository.onError()
                return@launch
            }

            with(result.productDetailsList!![0]) {
                productDetails = this
                val proSubscriptions = pricingMapper.map(this)
                billingStateRepository.update(proSubscriptions)
            }
        }
    }

    private suspend fun ensureConnection() {
        var counter = 0
        while (!connectionState.get()) {
            delay(counter++ * DELAY_THRESHOLD)
            startConnection()
        }
    }

    override fun subscribe(period: Subscription.Period) {
        // TODO: Add exception handler to write in logs
        mainScope.launch {
            ensureConnection()
            val details = productDetails
            checkNotNull(details) { "Product details are not yet loaded" }

            val offerToken =
                details.subscriptionOfferDetails?.find { subsDetails ->
                    subsDetails.pricingPhases.pricingPhaseList.any { phase ->
                        phase.billingPeriod == periodMapper.map(period)
                    }
                }?.offerToken
            checkNotNull(offerToken) { "No required subscription found" }

            val productDetailsParamsList =
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .setOfferToken(offerToken)
                        .build(),
                )

            val userId = userRepository.get().first { !it?.id.isNullOrBlank() }?.id

            val billingFlowParams =
                BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .setObfuscatedAccountId(userId ?: throw IllegalStateException("We can't fetch user for this purchase!"))
                    .build()

            val activity = activityProvider.activeActivity
            checkNotNull(activity) { "No live activity to start purchase" }

            billingClient.value.launchBillingFlow(activity, billingFlowParams)
        }
    }

    override fun isBillingSupported(): Boolean = true

    private fun startConnection() {
        billingClient.value.startConnection(
            object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        analytics.log("Billing connection established", LogLevel.DEBUG, TAG)
                        connectionState.set(true)
                    } else if (billingResult.responseCode == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE) {
                        val exception = PaywallRestrictionException("Billing not available on device")
                        analytics.log(exception)

                        throw exception
                    }
                }

                override fun onBillingServiceDisconnected() {
                    analytics.log("Billing service disconnected", LogLevel.DEBUG, TAG)
                    connectionState.set(false)
                }
            },
        )
    }
}
