package app.wesplit.domain.model.group

import app.wesplit.domain.model.user.User

data class Group(
    val id: String,
    val title: String,
    val users: List<User>,
    val imageUrl: String? = null,
)
