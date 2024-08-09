package app.wesplit.domain.model

interface AnalyticsManager {
    fun log(exception: Exception)

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
