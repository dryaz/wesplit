package app.wesplit.domain.model.group

interface GroupRepository {
    suspend fun get(): List<Group>
}
