package app.wesplit.domain.model.group

import kotlinx.coroutines.flow.StateFlow

interface GroupRepository {
    fun get(): StateFlow<List<Group>>

    suspend fun get(groupId: String): Group?

    fun create()
}
