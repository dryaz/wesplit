import app.wesplit.domain.model.AppReviewManager
import app.wesplit.domain.model.ReviewType
import com.mikhailovskii.inappreview.ReviewCode
import com.mikhailovskii.inappreview.appStore.AppStoreInAppReviewInitParams
import com.mikhailovskii.inappreview.appStore.AppStoreInAppReviewManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

class IosAppReviewManager : AppReviewManager {
    private val reviewManager = AppStoreInAppReviewManager(AppStoreInAppReviewInitParams("6714482007"))

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun requestReview(type: ReviewType): Flow<Result<Unit>> =
        when (type) {
            ReviewType.IN_APP -> reviewManager.requestInAppReview().mapLatest { code -> result(code) }
            ReviewType.MARKET -> reviewManager.requestInMarketReview().mapLatest { code -> result(code) }
        }

    private fun result(code: ReviewCode) =
        if (code == ReviewCode.NO_ERROR) {
            Result.success(Unit)
        } else {
            Result.failure<Unit>(RuntimeException(code.name))
        }
}
