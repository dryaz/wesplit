import app.wesplit.CommonAnalyticsManager
import app.wesplit.domain.model.LogLevel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.analytics

private const val EXCEPTION_EVENT = "exception"
private const val LOG_EVENT = "log"
private const val MSG_PARAM = "msg"

class JsAnalyticsManager : CommonAnalyticsManager() {
    override fun log(throwable: Throwable) {
        throwable.printStackTrace()
        throwable.message?.let {
            Firebase.analytics.logEvent(EXCEPTION_EVENT, mapOf(MSG_PARAM to it))
        }
    }

    override fun log(
        msg: String,
        logLevel: LogLevel,
        tag: String,
    ) {
        if (logLevel != LogLevel.DEBUG) {
            Firebase.analytics.logEvent(LOG_EVENT, mapOf(MSG_PARAM to msg))
        } else {
            println("$tag | $logLevel | $msg")
        }
    }
}
