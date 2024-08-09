import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.LogLevel

// TODO: Integrate crashlytics/Amplitude/Firebase analytics
class CommonAnalyticsManager : AnalyticsManager {
    override fun log(exception: Exception) {
        exception.printStackTrace()
    }

    override fun log(
        msg: String,
        logLevel: LogLevel,
        tag: String,
    ) {
        println("Tag: $tag | Leve:$logLevel | $msg")
    }

    override fun track(event: String) {
        println("Event: $event")
    }

    override fun track(
        event: String,
        params: Map<String, String>,
    ) {
        println("Event: $event | ${params.map { "${it.key} : ${it.value}" }}")
    }

    override fun setUserId(userId: String) {
        println("Set user ID to $userId")
    }

    override fun setParam(
        key: String,
        value: String,
    ) {
        println("Set param: $key | $value")
    }
}
