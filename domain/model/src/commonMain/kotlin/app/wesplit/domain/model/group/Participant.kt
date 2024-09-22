package app.wesplit.domain.model.group

import app.wesplit.domain.model.user.User
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Paritipant is an enity that is a part of the group.
 * Same participant could be involved in multiple groups.
 */
@OptIn(ExperimentalUuidApi::class)
@Serializable
@SerialName("participant")
data class Participant(
    @SerialName("id")
    val id: String = Uuid.random().toString(),
    @SerialName("name")
    val name: String,
    @SerialName("user")
    val user: User? = null,
)

fun Participant.isMe(): Boolean {
    val uid = Firebase.auth.currentUser?.uid
    return uid != null && uid in (user?.authIds ?: emptySet())
}

fun Participant.isMe(group: Group): Boolean {
    val uid = Firebase.auth.currentUser?.uid
    val groupPartId =
        uid?.let { id ->
            group.participants.firstOrNull { id in (it.user?.authIds ?: emptyList()) }
        }?.id
    return this.id == groupPartId
}
