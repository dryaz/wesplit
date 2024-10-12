package app.wesplit

import app.wesplit.di.ActivityProvider
import app.wesplit.domain.model.AppReviewManager
import app.wesplit.domain.model.ReviewType
import com.mikhailovskii.inappreview.ReviewCode
import com.mikhailovskii.inappreview.googlePlay.GooglePlayInAppReviewInitParams
import com.mikhailovskii.inappreview.googlePlay.GooglePlayInAppReviewManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest

class AndroidAppReviewManager(
    private val activityProvider: ActivityProvider,
) : AppReviewManager {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun requestReview(type: ReviewType): Flow<Result<Unit>> {
        return activityProvider.activeActivity?.let {
            val reviewManager = GooglePlayInAppReviewManager(GooglePlayInAppReviewInitParams(it))
            when (type) {
                ReviewType.IN_APP -> reviewManager.requestInAppReview().mapLatest { code -> result(code) }
                ReviewType.MARKET -> reviewManager.requestInMarketReview().mapLatest { code -> result(code) }
            }
        } ?: flow { emit(Result.failure<Unit>(RuntimeException("Activity not found"))) }
    }

    private fun result(code: ReviewCode) =
        if (code == ReviewCode.NO_ERROR) {
            Result.success(Unit)
        } else {
            Result.failure<Unit>(RuntimeException(code.name))
        }
}
