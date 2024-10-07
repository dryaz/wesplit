package app.wesplit.domain.model

const val REVIEW_EVENT = "in_app_review"
const val REVIEW_SOURCE = "source"
const val REVIEW_TYPE = "type"

interface AppReviewManager {
    fun requestReview(type: ReviewType)
}

enum class ReviewType {
    IN_APP,
    MARKET,
}

object NotSupportedAppReviewManager : AppReviewManager {
    override fun requestReview(type: ReviewType) {
        print("In-app review not supported")
    }
}
