package app.wesplit.domain.model.group

import dev.gitlive.firebase.firestore.BaseTimestamp
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@SerialName("group")
data class Group(
    @Transient
    val id: String = "",
    @SerialName("title")
    val title: String,
    @SerialName("imageDescription")
    val imageDescription: String?,
    @SerialName("isImageGen")
    val isImageGen: Boolean = false,
    @SerialName("participants")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val participants: Set<Participant> = emptySet(),
    @SerialName("balances")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val balances: Balance? = null,
    @SerialName("imageUrl")
    val imageUrl: String? = null,
    @SerialName("createdAt")
    val createdAt: BaseTimestamp = Timestamp.ServerTimestamp,
    @SerialName("updatedAt")
    val updatedAt: BaseTimestamp = Timestamp.ServerTimestamp,
    // TODO: For security rules at the moment we also add auth uid in here, maybe need to split
    // TODO: Public shares could be done via this
    @SerialName("tokens")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val tokens: List<String> = emptyList(),
    @SerialName("publicToken")
    val publicToken: String,
)

fun Group.uiTitle() =
    title.ifBlank {
        participants.map { it.name }.joinToString()
    }
