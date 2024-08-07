package app.wesplit.domain.model.group

interface GroupRepository {
    suspend fun get(): List<Group>
    suspend fun get(groupId: String): Group
}
