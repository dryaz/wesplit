package app.wesplit.domain.model.group

data class Group(
    val id: String,
    val title: String,
    val users: List<Participant>,
    val imageUrl: String? = null,
)
