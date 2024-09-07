package app.wesplit.data.firebase

import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.group.Group
import app.wesplit.domain.model.group.GroupRepository
import app.wesplit.domain.model.group.Participant
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
        participants: Set<Participant>,
    ) {
        val eventName = if (id != null) GROUP_UPDATE_EVENT else GROUP_CREATE_EVENT

        analyticsManager.track(
            eventName,
            mapOf(
                GROUP_COMMIT_PARAM_USERS to participants.size.toString(),
                GROUP_COMMIT_PARAM_TITLE to title,
            ),
        )

        groups.getAndUpdate { existingGroups ->
            // TODO: Support updating for internal memory impl
            val randItn = Random.nextInt()
            existingGroups + Group(randItn.toString(), "$title", participants)
        }
    }

    // TODO: Implement, current is mock
    override fun getSuggestedParticipants(searchQuery: String): Flow<List<Participant>> =
        flow {
            val data =
                listOf(
                    Participant("1", "Ivan", User("1", "Ivan", "https://randomuser.me/api/portraits/med/men/75.jpg")),
                    Participant("2", "Dima", User("2", "Dima", "https://randomuser.me/api/portraits/med/men/75.jpg")),
                    Participant("3", "Marko", User("3", "Marko", "https://randomuser.me/api/portraits/med/men/75.jpg")),
                    Participant("4", "John Smith", User("4", "John Smith", "https://randomuser.me/api/portraits/med/men/75.jpg")),
                    Participant("5", "Pedro", User("5", "Pedro", "https://randomuser.me/api/portraits/med/men/75.jpg")),
                    Participant("6", "Huan Gonsales", User("6", "Huan Gonsales", "https://randomuser.me/api/portraits/med/men/75.jpg")),
                )
            // TODO: Probably we soulc query firebase with some query
            emit(data.filter { !it.name.isNullOrBlank() && it.name.lowercase().contains(searchQuery.lowercase()) })
        }
}
