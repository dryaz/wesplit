import app.wesplit.domain.model.AppReviewManager
import app.wesplit.domain.model.ReviewType
import com.mikhailovskii.inappreview.appStore.AppStoreInAppReviewInitParams
import com.mikhailovskii.inappreview.appStore.AppStoreInAppReviewManager

class IosAppReviewManager : AppReviewManager {
    private val reviewManager = AppStoreInAppReviewManager(AppStoreInAppReviewInitParams("6714482007"))

    override fun requestReview(type: ReviewType) {
        when (type) {
            ReviewType.IN_APP -> reviewManager.requestInAppReview()
            ReviewType.MARKET -> reviewManager.requestInMarketReview()
        }
    }
}
