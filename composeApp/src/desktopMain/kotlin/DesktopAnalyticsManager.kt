import app.wesplit.CommonAnalyticsManager
import app.wesplit.domain.model.LogLevel

private const val EXCEPTION_EVENT = "exception"
private const val LOG_EVENT = "log"
private const val MSG_PARAM = "msg"

// TODO: Firebase.analytics not implemented on deskptop :(
class DesktopAnalyticsManager : CommonAnalyticsManager() {
    override fun log(throwable: Throwable) {
//        throwable.message?.let {
//            Firebase.analytics.logEvent(EXCEPTION_EVENT, mapOf(MSG_PARAM to it))
//        }
        throwable.printStackTrace()
    }

    override fun log(
        msg: String,
        logLevel: LogLevel,
        tag: String,
    ) {
//        if (logLevel != LogLevel.DEBUG) {
//            Firebase.analytics.logEvent(LOG_EVENT, mapOf(MSG_PARAM to msg))
//        } else {
//            println("$tag | $logLevel | $msg")
//        }
        println("$tag | $logLevel | $msg")
    }

    override fun track(event: String) {
//        Firebase.analytics.logEvent(event)
        println("$event")
    }

    override fun track(
        event: String,
        params: Map<String, String>,
    ) {
//        Firebase.analytics.logEvent(event, params)
        println("$event | $params")
    }

    override fun setUserId(userId: String) {
//        Firebase.analytics.setUserId(userId)
        println("Set user id: $userId")
    }

    override fun setParam(
        key: String,
        value: String,
    ) {
//        Firebase.analytics.setUserProperty(key, value)
        println("Set user pro: $key | $value")
    }
}
