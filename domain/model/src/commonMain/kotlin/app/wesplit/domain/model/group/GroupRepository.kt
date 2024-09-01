package app.wesplit.domain.model.group

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface GroupRepository {
    fun get(): StateFlow<List<Group>>

    // TODO: Flow? Cause first cached thing could be returned.

    /**
     * Get group by id. First cached result could be returned.
     * Also try to retrieve group from cloud and Result could be unsuccessfull in case of
     * e.g. network error or current user is not authorized to access the group.
     */
    fun get(groupId: String): Flow<Result<Group>>

    // TODO: Support image
    // TODO: Define if current user must be in the users list

    /**
     * Persist data in service.
     *
     * @param id if null passed new group will be created.
     */
    fun commit(
        id: String?,
        title: String,
        participants: List<Participant>,
    )

    /**
     * Get suggestions about users when user suppose to add new user to the group.
     */
    fun getSuggestedParticipants(searchQuery: String): Flow<List<Participant>>
}
