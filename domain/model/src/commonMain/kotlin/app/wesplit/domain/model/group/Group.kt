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
)

fun Group.uiTitle() =
    title.ifBlank {
        participants.map { it.name }.joinToString()
    }
