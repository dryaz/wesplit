package app.wesplit

import app.wesplit.di.ActivityProvider
import app.wesplit.domain.model.AppReviewManager
import app.wesplit.domain.model.ReviewType
import com.mikhailovskii.inappreview.googlePlay.GooglePlayInAppReviewInitParams
import com.mikhailovskii.inappreview.googlePlay.GooglePlayInAppReviewManager

class AndroidAppReviewManager(
    private val activityProvider: ActivityProvider,
) : AppReviewManager {
    override fun requestReview(type: ReviewType) {
        activityProvider.activeActivity?.let {
            val reviewManager = GooglePlayInAppReviewManager(GooglePlayInAppReviewInitParams(it))
            when (type) {
                ReviewType.IN_APP -> reviewManager.requestInAppReview()
                ReviewType.MARKET -> reviewManager.requestInMarketReview()
            }
        }
    }
}
