package app.wesplit.data.firebase

import app.wesplit.domain.model.group.Group
import app.wesplit.domain.model.group.GroupRepository
import org.koin.core.annotation.Single

@Single
class GroupFirebaseRepository : GroupRepository {
    override suspend fun get(): List<Group> =
        listOf(
            Group("1", "Awesome group 1", null, emptyList()),
            Group("2", "Awesome group 2", null, emptyList()),
            Group("3", "Awesome group 3", null, emptyList()),
            Group("4", "Awesome group 4", null, emptyList()),
            Group("5", "Awesome group 5", null, emptyList()),
        )

    override suspend fun get(groupId: String): Group =
        Group(
            groupId,
            "Group with id: $groupId",
            null,
            emptyList(),
        )
}
