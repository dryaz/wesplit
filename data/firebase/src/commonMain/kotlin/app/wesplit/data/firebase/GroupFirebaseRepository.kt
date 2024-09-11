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
                    Participant("1", "Ivan", User("1", "Ivan", "https://randomuser.me/api/portraits/med/men/74.jpg")),
                    Participant("2", "Dima", User("2", "Dima", "https://randomuser.me/api/portraits/med/men/73.jpg")),
                    Participant("3", "Marko", User("3", "Marko", "https://randomuser.me/api/portraits/med/men/72.jpg")),
                    Participant("4", "John Smith", User("4", "John Smith", "https://randomuser.me/api/portraits/med/men/71.jpg")),
                    Participant("5", "Pedro", User("5", "Pedro", "https://randomuser.me/api/portraits/med/men/70.jpg")),
                    Participant("6", "Huan Gonsales", User("6", "Huan Gonsales", "https://randomuser.me/api/portraits/med/men/75.jpg")),
                    Participant("7", "Daria", User("7", "Ivan", "https://randomuser.me/api/portraits/med/men/61.jpg")),
                    Participant("8", "Tanya", User("8", "Dima", "https://randomuser.me/api/portraits/med/men/60.jpg")),
                    Participant("9", "Anna", User("9", "Marko", "https://randomuser.me/api/portraits/med/men/59.jpg")),
                    Participant("10", "Zarina", User("10", "John Smith", "https://randomuser.me/api/portraits/med/men/58.jpg")),
                    Participant("11", "Jim", User("11", "Pedro", "https://randomuser.me/api/portraits/med/men/57.jpg")),
                    Participant("12", "James", User("12", "Huan Gonsales", "https://randomuser.me/api/portraits/med/men/56.jpg")),
                    Participant("13", "Petr", User("13", "Ivan", "https://randomuser.me/api/portraits/med/men/55.jpg")),
                    Participant("14", "Vlad", User("14", "Dima", "https://randomuser.me/api/portraits/med/men/54.jpg")),
                    Participant("15", "Nikola", User("15", "Marko", "https://randomuser.me/api/portraits/med/men/53.jpg")),
                    Participant("16", "Nobody", User("16", "John Smith", "https://randomuser.me/api/portraits/med/men/52.jpg")),
                    Participant("17", "Othe name's", User("17", "Pedro", "https://randomuser.me/api/portraits/med/men/51.jpg")),
                    Participant("18", "Last item", User("18", "Huan Gonsales", "https://randomuser.me/api/portraits/med/men/50.jpg")),
                )
            // TODO: Probably we soulc query firebase with some query
            emit(data.filter { !it.name.isNullOrBlank() && it.name.lowercase().contains(searchQuery.lowercase()) })
        }
}
