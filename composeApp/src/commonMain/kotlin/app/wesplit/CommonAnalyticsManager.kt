package app.wesplit

import app.wesplit.domain.model.AnalyticsManager
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.analytics

abstract class CommonAnalyticsManager : AnalyticsManager {
    override fun track(event: String) {
        Firebase.analytics.logEvent(event)
    }

    override fun track(
        event: String,
        params: Map<String, String>,
    ) {
        Firebase.analytics.logEvent(event, params)
    }

    override fun setUserId(userId: String) {
        Firebase.analytics.setUserId(userId)
    }

    override fun setParam(
        key: String,
        value: String,
    ) {
        Firebase.analytics.setUserProperty(key, value)
    }
}
