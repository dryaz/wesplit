package app.wesplit.domain.model

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

const val REVIEW_EVENT = "in_app_review"
const val REVIEW_SOURCE = "source"
const val REVIEW_TYPE = "type"
const val REVIEW_RESULT = "result"

interface AppReviewManager {
    fun requestReview(type: ReviewType): Flow<Result<Unit>>
}

enum class ReviewType {
    IN_APP,
    MARKET,
}

object NotSupportedAppReviewManager : AppReviewManager {
    override fun requestReview(type: ReviewType): Flow<Result<Unit>> =
        flow {
            emit(Result.failure(RuntimeException("In-app review not supported")))
        }
}
