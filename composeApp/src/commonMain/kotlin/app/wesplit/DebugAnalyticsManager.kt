package app.wesplit

import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.LogLevel

class DebugAnalyticsManager : AnalyticsManager {
    override fun log(throwable: Throwable) {
        throwable.printStackTrace()
    }

    override fun log(
        msg: String,
        logLevel: LogLevel,
        tag: String,
    ) {
        println("$tag: $logLevel | $msg")
    }

    override fun track(event: String) {
        println(event)
    }

    override fun track(
        event: String,
        params: Map<String, String>,
    ) {
        println("$event\n${params.map { "${it.key} : ${it.value}" }}")
    }

    override fun setUserId(userId: String) {
        println("Set userId: $userId")
    }

    override fun setParam(
        key: String,
        value: String,
    ) {
        println("Set $key : $value")
    }
}
