package app.wesplit.domain.model

// TODO: POC like in timber split logging and analytics. Let use Kermit.log to record crashlytics logs for crashes.
interface AnalyticsManager {
    fun log(throwable: Throwable)

    fun log(
        msg: String,
        logLevel: LogLevel,
        tag: String = "WeSplitCommonLog",
    )

    fun track(event: String)

    fun track(
        event: String,
        params: Map<String, String>,
    )

    fun setUserId(userId: String)

    fun setParam(
        key: String,
        value: String,
    )
}

enum class LogLevel {
    DEBUG,
    WARNING,
    ERROR,
}
