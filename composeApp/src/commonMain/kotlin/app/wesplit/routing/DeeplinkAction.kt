package app.wesplit.routing

import com.motorro.keeplink.deeplink.Action
import com.motorro.keeplink.uri.data.Param
import com.motorro.keeplink.uri.data.PshComponents
import com.motorro.keeplink.uri.data.of
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

    class QuickSplit : DeeplinkAction() {
        companion object {
            const val SEGMENT = "quickSplit"
        }

        override fun getPath(): Array<String> = super.getPath() + SEGMENT
    }

    sealed class Group : DeeplinkAction() {
        abstract val groupId: String

        internal companion object {
            const val SEGMENT = "group"
        }

        override fun getPath(): Array<String> = super.getPath() + SEGMENT

        class Details(override val groupId: String, val token: String?) : Group() {
            internal companion object {
                const val TOKEN = "token"
            }

            override fun getPath(): Array<String> = super.getPath() + groupId

            override fun getSearch(): Array<Param> {
                return if (token != null) {
                    super.getSearch() +
                        arrayOf(
                            TOKEN of token,
                        )
                } else {
                    super.getSearch()
                }
            }
        }

        class Expense(override val groupId: String, val expenseId: String? = null) : Group() {
            internal companion object {
                const val SEGMENT = "expense"
                const val TOKEN = "token"
            }

            override fun getPath(): Array<String> = super.getPath() + groupId + SEGMENT + (expenseId ?: "")
        }
    }

    class Unknown(private val components: PshComponents) : DeeplinkAction() {
        override fun getPath(): Array<String> = components.getPath()

        override fun getSearch(): Array<Param> = components.getSearch()

        override fun getHash(): String = components.getHash()

        override val isValid: Boolean = false
    }
}
