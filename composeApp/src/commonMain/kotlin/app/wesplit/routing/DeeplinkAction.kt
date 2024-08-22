package app.wesplit.routing

import com.motorro.keeplink.deeplink.Action
import com.motorro.keeplink.uri.data.Param
import com.motorro.keeplink.uri.data.PshComponents
import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@JsExport
@Serializable
@OptIn(ExperimentalJsExport::class)
sealed class DeeplinkAction : Action() {
    class Home : DeeplinkAction() {
        companion object {
            const val SEGMENT = ""
        }

        override fun getPath(): Array<String> = super.getPath() + SEGMENT
    }

    class Profile : DeeplinkAction() {
        companion object {
            const val SEGMENT = "profile"
        }

        override fun getPath(): Array<String> = super.getPath() + SEGMENT
    }

    class GroupDetails(val groupId: String) : DeeplinkAction() {
        companion object {
            const val SEGMENT = "group"
        }

        override fun getPath(): Array<String> = super.getPath() + SEGMENT + groupId
    }

    class Unknown(private val components: PshComponents) : DeeplinkAction() {
        override fun getPath(): Array<String> = components.getPath()

        override fun getSearch(): Array<Param> = components.getSearch()

        override fun getHash(): String = components.getHash()

        override val isValid: Boolean = false
    }
}
