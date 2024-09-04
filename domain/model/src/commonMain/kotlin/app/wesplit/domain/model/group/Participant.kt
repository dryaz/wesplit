package app.wesplit.domain.model.group

import app.wesplit.domain.model.user.User

/**
 * Paritipant is an enity that is a part of the group.
 * Same participant could be involved in multiple groups.
 */
data class Participant(
    val id: String?,
    val name: String,
    val user: User? = null,
    val isMe: Boolean = false,
)
