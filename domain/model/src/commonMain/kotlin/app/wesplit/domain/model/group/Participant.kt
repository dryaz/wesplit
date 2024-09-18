package app.wesplit.domain.model.group

import app.wesplit.domain.model.user.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Paritipant is an enity that is a part of the group.
 * Same participant could be involved in multiple groups.
 */
@Serializable
@SerialName("participant")
data class Participant(
    @Transient
    val id: String? = null,
    @SerialName("name")
    val name: String,
    @SerialName("user")
    val user: User? = null,
    @Transient
    val isMe: Boolean = false,
)
