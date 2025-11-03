package app.wesplit

actual fun isDebugEnvironment(): Boolean {
    // iOS doesn't have BuildConfig, so check if running in debug mode
    // In a real scenario, this could check #if DEBUG from Swift
    return true // For now, always show debug button on iOS
}

