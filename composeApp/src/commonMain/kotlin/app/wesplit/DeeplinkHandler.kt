package app.wesplit

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class DeepLinkHandler {
    val deeplink = MutableStateFlow("")

    fun handleDeeplink(url: String) {
        deeplink.update { url }
    }

    fun consume() {
        deeplink.update { "" }
    }
}
