package app.wesplit

import android.util.Log
import app.wesplit.domain.model.LogLevel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.analytics
import dev.gitlive.firebase.crashlytics.crashlytics

class AndroidAnalyticsManager : CommonAnalyticsManager() {
    override fun log(throwable: Throwable) {
        if (BuildConfig.DEBUG) {
            throwable.printStackTrace()
        }

        Firebase.crashlytics.recordException(throwable)
    }

    override fun log(
        msg: String,
        logLevel: LogLevel,
        tag: String,
    ) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, msg)
        }

        if (logLevel != LogLevel.DEBUG) {
            Firebase.crashlytics.log(msg)
        }
    }

    override fun setUserId(userId: String) {
        super.setUserId(userId)
        Firebase.crashlytics.setUserId(userId)
    }

    override fun track(event: String) {
        super.track(event)
        if (BuildConfig.DEBUG) {
            Log.e("EVENT", event)
        }

        Firebase.crashlytics.log(event)
    }

    override fun track(
        event: String,
        params: Map<String, String>,
    ) {
        super.track(event, params)
        if (BuildConfig.DEBUG) {
            Log.e("EVENT", event)
        }

        Firebase.crashlytics.log(event)
    }

    override fun setParam(
        key: String,
        value: String,
    ) {
        Firebase.analytics.setUserProperty(key, value)
    }
}
