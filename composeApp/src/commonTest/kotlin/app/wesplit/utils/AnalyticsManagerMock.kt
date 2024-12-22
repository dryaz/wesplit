package app.wesplit.utils

import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.LogLevel

class AnalyticsManagerMock : AnalyticsManager {
    override fun log(throwable: Throwable) {}

    override fun log(
        msg: String,
        logLevel: LogLevel,
        tag: String,
    ) {}

    override fun track(event: String) {}

    override fun track(
        event: String,
        params: Map<String, String>,
    ) {}

    override fun setUserId(userId: String) {}

    override fun setParam(
        key: String,
        value: String,
    ) {}
}
