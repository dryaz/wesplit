package app.wesplit

import kotlinx.browser.window

actual fun isDebugEnvironment(): Boolean {
    val hostname = window.location.hostname
    return hostname == "localhost" || hostname == "127.0.0.1"
}

