package app.wesplit.data.firebase

import app.wesplit.domain.model.group.Group
import app.wesplit.domain.model.group.GroupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.getAndUpdate
import org.koin.core.annotation.Single
import kotlin.random.Random

@Single
class GroupFirebaseRepository : GroupRepository {
    private val groups = MutableStateFlow<List<Group>>(emptyList())

    override fun get(): StateFlow<List<Group>> = groups

    override suspend fun get(groupId: String): Group? =
        groups.value.find { group ->
            group.id == groupId
        }

    override fun create() {
        groups.getAndUpdate { existingGroups ->
            val randItn = Random.nextInt()
            existingGroups +
                Group(
                    randItn.toString(), "Group $randItn", null, emptyList(),
                )
        }
    }
}
