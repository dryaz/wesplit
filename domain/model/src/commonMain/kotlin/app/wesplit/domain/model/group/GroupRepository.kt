package app.wesplit.domain.model.group

import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    fun get(): Flow<List<Group>>

    // TODO: Flow? Cause first cached thing could be returned.

    /**
     * Get group by id. First cached result could be returned.
     * Also try to retrieve group from cloud and Result could be unsuccessfull in case of
     * e.g. network error or current user is not authorized to access the group.
     *
     * @param token If provided user should get access to the group.
     *      If not this token is used to add access for current user.
     */
    fun get(
        groupId: String,
        token: String? = null,
    ): Flow<Result<Group>>

    // TODO: Support image
    // TODO: Define if current user must be in the users list

    /**
     * Persist data in service.
     *
     * @param id if null passed new group will be created.
     */
    suspend fun commit(
        id: String?,
        title: String,
        imageDescription: String,
        participants: Set<Participant>,
        imageUrl: String?,
    )

    suspend fun leave(groupId: String)

    /**
     * Get suggestions about users when user suppose to add new user to the group.
     */
    fun getSuggestedParticipants(searchQuery: String): Flow<List<Participant>>
}
