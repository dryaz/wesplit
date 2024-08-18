package app.wesplit

import app.wesplit.domain.model.LogLevel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.crashlytics.crashlytics

class AndroidAnalyticsManager : CommonAnalyticsManager() {
    override fun log(throwable: Throwable) {
        Firebase.crashlytics.recordException(throwable)
    }

    override fun log(
        msg: String,
        logLevel: LogLevel,
        tag: String,
    ) {
        if (logLevel != LogLevel.DEBUG) Firebase.crashlytics.log(msg)
    }

    override fun setUserId(userId: String) {
        super.setUserId(userId)
        Firebase.crashlytics.setUserId(userId)
    }

    override fun track(event: String) {
        super.track(event)
        Firebase.crashlytics.log(event)
    }

    override fun track(
        event: String,
        params: Map<String, String>,
    ) {
        super.track(event, params)
        Firebase.crashlytics.log(event)
    }
}
