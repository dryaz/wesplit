package app.wesplit.data.firebase

import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.account.Account
import app.wesplit.domain.model.account.AccountRepository
import app.wesplit.domain.model.group.Group
import app.wesplit.domain.model.group.GroupRepository
import app.wesplit.domain.model.group.Participant
import app.wesplit.domain.model.group.isMe
import app.wesplit.domain.model.user.User
import app.wesplit.domain.model.user.UserRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.FirebaseFirestoreException
import dev.gitlive.firebase.firestore.FirestoreExceptionCode
import dev.gitlive.firebase.firestore.ServerTimestampBehavior
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.code
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

val GROUP_COLLECTION = "groups"

private const val TOKENS_FIELD = "tokens"
private const val PUBLIC_TOKEN_FIELD = "publicToken"
private const val PARTICIPANTS_FIELD = "participants"

private const val GROUP_CREATE_EVENT = "group_create"
private const val GROUP_UPDATE_EVENT = "group_update"
private const val GROUP_LEAVE_EVENT = "group_leave"
private const val GROUP_JOIN_EVENT = "group_join"
private const val GROUP_COMMIT_PARAM_TITLE = "title"
private const val GROUP_COMMIT_PARAM_USERS = "users_num"

@Single
class GroupFirebaseRepository(
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository,
    private val coroutineDispatcher: CoroutineDispatcher,
    private val analyticsManager: AnalyticsManager,
) : GroupRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun get(): Flow<List<Group>> =
        accountRepository.get().flatMapLatest { auth ->
            when (auth) {
                Account.Anonymous,
                Account.Unknown,
                Account.Restricted, // TODO: In case of restriction to group -> return this gropu in list
                -> flow { emptyList<List<Group>>() }

                is Account.Authorized ->
                    Firebase.firestore.collection(GROUP_COLLECTION).where {
                        TOKENS_FIELD contains (Firebase.auth.currentUser?.uid ?: "")
                    }.snapshots.map {
                        withContext(coroutineDispatcher) {
                            it.documents.map {
                                it.data(Group.serializer(), ServerTimestampBehavior.ESTIMATE).copy(
                                    id = it.id,
                                )
                            }
                        }
                    }
            }
        }

    override fun get(
        groupId: String,
        token: String?,
    ): Flow<Result<Group>> {
        return Firebase.firestore.collection(GROUP_COLLECTION).document(groupId).snapshots.map {
            if (it.exists) {
                val group = it.data(Group.serializer(), ServerTimestampBehavior.ESTIMATE)
                Result.success(group.copy(id = it.id))
            } else {
                val exception = NullPointerException("No group found with id: $groupId")
                analyticsManager.log(exception)
                // TODO: Check what if there is not enough permission
                Result.failure(exception)
            }
        }.retryWhen { cause, attempt ->
            if (attempt > 3) return@retryWhen false
            cause.printStackTrace()
            analyticsManager.log(cause)
            return@retryWhen if (cause is FirebaseFirestoreException &&
                cause.code == FirestoreExceptionCode.PERMISSION_DENIED
            ) {
                val uid = Firebase.auth.currentUser?.uid
                if (uid != null && token != null) {
                    Firebase.firestore.collection(GROUP_COLLECTION).document(groupId).update(
                        mapOf(
                            TOKENS_FIELD to FieldValue.arrayUnion(uid),
                            PUBLIC_TOKEN_FIELD to token,
                        ),
                    )
                }
                true
            } else {
                false
            }
        }
    }

    // TODO: Add currency to UI + DB, one currency set per group as of now.
    //  Multiple currencies + FX sholuld be a premium feature.
    @OptIn(ExperimentalUuidApi::class)
    override suspend fun commit(
        id: String?,
        title: String,
        imageDescription: String,
        participants: Set<Participant>,
        imageUrl: String?,
    ): Unit =
        withContext(coroutineDispatcher + NonCancellable) {
            val eventName = if (id != null) GROUP_UPDATE_EVENT else GROUP_CREATE_EVENT

            analyticsManager.track(
                eventName,
                mapOf(
                    GROUP_COMMIT_PARAM_USERS to participants.size.toString(),
                    GROUP_COMMIT_PARAM_TITLE to title,
                ),
            )

            if (id == null) {
                // TODO: Extract firestore logic to groupDataSource 'cause repository will behave as usecase,
                //  e.g. when user creates group all participants should be treated as contacts and also
                //  stored in user relations as possible contacts to fetch in future!
                //  If not extract -> hard to test ==> cover with tests!
                val publicToken = Uuid.random().toString()
                val newGroup =
                    Group(
                        title = title,
                        participants =
                            participants.map { participant ->
                                if (participant.user != null) {
                                    participant
                                } else {
                                    participant.copy(
                                        user =
                                            User(
                                                name = participant.name,
                                            ),
                                    )
                                }
                            }.toSet(),
                        createdAt = Timestamp.ServerTimestamp,
                        tokens = participants.flatMap { it.user?.authIds ?: emptyList() } + publicToken,
                        publicToken = publicToken,
                        imageUrl = imageUrl,
                        imageDescription = imageDescription,
                        isImageGen = imageDescription.isNotBlank(),
                    )
                Firebase.firestore.collection(GROUP_COLLECTION).add(
                    strategy = Group.serializer(),
                    data = newGroup,
                )
            } else {
                val doc = Firebase.firestore.collection(GROUP_COLLECTION).document(id).get()
                if (doc.exists) {
                    val existingGroup = doc.data(Group.serializer(), ServerTimestampBehavior.ESTIMATE)
                    Firebase.firestore.collection(GROUP_COLLECTION).document(id).update(
                        strategy = Group.serializer(),
                        data =
                            Group(
                                title = title,
                                participants =
                                    participants.map { participant ->
                                        if (participant.user != null) {
                                            participant
                                        } else {
                                            participant.copy(
                                                user =
                                                    User(
                                                        name = participant.name,
                                                    ),
                                            )
                                        }
                                    }.toSet(),
                                createdAt = existingGroup.createdAt,
                                tokens = participants.flatMap { it.user?.authIds ?: emptyList() } + existingGroup.publicToken,
                                publicToken = existingGroup.publicToken,
                                imageUrl = imageUrl,
                                imageDescription = imageDescription,
                                isImageGen = existingGroup.imageDescription != imageDescription,
                            ),
                    )
                } else {
                    // TODO: Fire back and error to ui
                    val exception = IllegalStateException("Try to edit group document with id $id which not exists")
                    analyticsManager.log(exception)
                }
            }
        }

    override suspend fun leave(groupId: String) {
        analyticsManager.track(GROUP_LEAVE_EVENT)
        val doc = Firebase.firestore.collection(GROUP_COLLECTION).document(groupId).get()
        if (doc.exists) {
            val existingGroup = doc.data(Group.serializer(), ServerTimestampBehavior.ESTIMATE)
            val myTokens =
                (
                    existingGroup.participants.firstOrNull { it.isMe() }?.user?.authIds
                        ?: emptyList()
                ) + (userRepository.get().value?.authIds ?: emptyList())
            val newParticipants = existingGroup.participants.filterNot { it.isMe() }.toSet()

            val newTokens = existingGroup.tokens.filterNot { it in myTokens }
            val batch = Firebase.firestore.batch()
            // TODO: Check if it's accurate (spoiler: probably not)
            val groupRef = Firebase.firestore.collection(GROUP_COLLECTION).document(groupId)
            if (newTokens.isNotEmpty()) {
                batch.update(
                    documentRef = groupRef,
                    mapOf(TOKENS_FIELD to newTokens),
                )
            } else {
                batch.update(
                    documentRef = groupRef,
                    mapOf(TOKENS_FIELD to FieldValue.delete),
                )
            }

            if (newParticipants.isNotEmpty()) {
                batch.update(
                    documentRef = groupRef,
                    mapOf(PARTICIPANTS_FIELD to newParticipants),
                )
            } else {
                batch.update(
                    documentRef = groupRef,
                    mapOf(PARTICIPANTS_FIELD to FieldValue.delete),
                )
            }

            batch.commit()
        }
    }

    // TODO: Implement, current is mock
    override fun getSuggestedParticipants(searchQuery: String): Flow<List<Participant>> =
        flow {
            emit(emptyList())
        }
}
