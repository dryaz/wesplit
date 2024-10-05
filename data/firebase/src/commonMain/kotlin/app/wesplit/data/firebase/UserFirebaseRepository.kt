package app.wesplit.data.firebase

import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.user.Contact
import app.wesplit.domain.model.user.Setting
import app.wesplit.domain.model.user.User
import app.wesplit.domain.model.user.UserRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.ServerTimestampBehavior
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single

private const val USER_COLLECTION = "users"

@Single
class UserFirebaseRepository(
    private val analytics: AnalyticsManager,
    private val coroutineDispatcher: CoroutineDispatcher,
) : UserRepository {
    private val coroutinScope = CoroutineScope(coroutineDispatcher)

    // TODO: Push 'upate' from account repo?
    //  But we have userRepo ref from accountRepo already == circular dep.
    @OptIn(ExperimentalCoroutinesApi::class)
    private val userFlow: StateFlow<User?> =
        Firebase.auth.authStateChanged.flatMapLatest { user ->
            get(user)
        }.stateIn(
            scope = coroutinScope,
            started = SharingStarted.Lazily,
            initialValue = null,
        )

    override fun get(): StateFlow<User?> = userFlow

    override fun update(setting: Setting) {
        userFlow.value?.let { authUser ->
            coroutinScope.launch {
                withContext(NonCancellable) {
                    when (setting) {
                        is Setting.Currency ->
                            Firebase.firestore.collection(USER_COLLECTION).document(authUser.id).update(
                                authUser.copy(
                                    lastUsedCurrency = setting.code,
                                ),
                            )
                    }
                }
            }
        }
    }

    override suspend fun delete() {
        userFlow.value?.let {
            Firebase.firestore.collection(USER_COLLECTION).document(it.id).delete()
        }
    }

    private suspend fun get(authUser: FirebaseUser?): Flow<User?> {
        if (authUser == null) {
            return flow { emit(null) }
        }

        if (authUser.uid.startsWith("group")) {
            return flow { emit(null) }
        }

        val doc = Firebase.firestore.collection(USER_COLLECTION).document(authUser.uid).get()
        if (!doc.exists) {
            val contactList = mutableListOf<Contact>()
            authUser.email?.let {
                if (!it.isNullOrBlank()) contactList.add(Contact.Email(it))
            }
            authUser.phoneNumber?.let {
                if (!it.isNullOrBlank()) contactList.add(Contact.Phone(it))
            }

            val newUser =
                User(
                    name = authUser.displayName ?: authUser.email ?: authUser.phoneNumber ?: authUser.uid,
                    photoUrl = authUser.photoURL,
                    contacts = contactList,
                    authIds = listOf(authUser.uid),
                )

            Firebase.firestore.collection(USER_COLLECTION).document(authUser.uid).set(User.serializer(), newUser)
        }

        return Firebase.firestore.collection(USER_COLLECTION).document(authUser.uid).snapshots.map {
            withContext(coroutineDispatcher) {
                it.data(User.serializer(), ServerTimestampBehavior.ESTIMATE).copy(
                    id = it.id,
                )
            }
        }
    }
}
