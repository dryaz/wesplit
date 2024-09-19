package app.wesplit.domain.model.group

import dev.gitlive.firebase.firestore.BaseTimestamp
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("group")
data class Group(
    @Transient
    val id: String = "",
    @SerialName("title")
    val title: String,
    @SerialName("participants")
    val participants: Set<Participant>,
    @SerialName("imageUrl")
    val imageUrl: String? = null,
    @SerialName("createdAt")
    val createdAt: BaseTimestamp = Timestamp.ServerTimestamp,
    @SerialName("updatedAt")
    val updatedAt: BaseTimestamp = Timestamp.ServerTimestamp,
    // TODO: For security rules at the moment we also add auth uid in here, maybe need to split
    // TODO: Public shares could be done via this
    @SerialName("tokens")
    val tokens: List<String> = emptyList(),
)

fun Group.uiTitle() =
    title.ifBlank {
        participants.map { it.name }.joinToString()
    }
