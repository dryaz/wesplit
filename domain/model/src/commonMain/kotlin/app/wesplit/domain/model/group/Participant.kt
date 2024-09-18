package app.wesplit.domain.model.group

import app.wesplit.domain.model.user.User
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Paritipant is an enity that is a part of the group.
 * Same participant could be involved in multiple groups.
 */
@Serializable
@SerialName("participant")
data class Participant(
    @SerialName("name")
    val name: String,
    @SerialName("user")
    val user: User? = null,
)

fun Participant.isMe(): Boolean {
    val uid = Firebase.auth.currentUser?.uid
    return uid != null && uid in (user?.authIds ?: emptySet())
}
