package app.wesplit.data.firebase

import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.group.Group
import app.wesplit.domain.model.group.GroupRepository
import app.wesplit.domain.model.user.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.getAndUpdate
import org.koin.core.annotation.Single
import kotlin.random.Random

private const val GROUP_CREATE_EVENT = "group_create"
private const val GROUP_UPDATE_EVENT = "group_update"
private const val GROUP_COMMIT_PARAM_TITLE = "title"
private const val GROUP_COMMIT_PARAM_USERS = "users_num"

@Single
class GroupFirebaseRepository(
    private val analyticsManager: AnalyticsManager,
) : GroupRepository {
    private val groups = MutableStateFlow<List<Group>>(emptyList())

    override fun get(): StateFlow<List<Group>> = groups

    override fun get(groupId: String): Flow<Result<Group>> =
        flow {
            val existingGroup = groups.value.find { it.id == groupId }
            if (existingGroup != null) {
                emit(Result.success(existingGroup))
            } else {
                emit(Result.failure(NullPointerException("No group found")))
            }
        }

    override fun commit(
        id: String?,
        title: String,
        users: List<User>,
    ) {
        val eventName = if (id != null) GROUP_UPDATE_EVENT else GROUP_CREATE_EVENT

        analyticsManager.track(
            eventName,
            mapOf(
                GROUP_COMMIT_PARAM_USERS to users.size.toString(),
                GROUP_COMMIT_PARAM_TITLE to title,
            ),
        )

        groups.getAndUpdate { existingGroups ->
            // TODO: Support updating for internal memory impl
            val randItn = Random.nextInt()
            existingGroups + Group(randItn.toString(), "$title $randItn", users)
        }
    }
}
