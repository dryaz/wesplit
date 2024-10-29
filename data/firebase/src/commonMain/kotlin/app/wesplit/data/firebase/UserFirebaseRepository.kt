package app.wesplit.data.firebase

import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.KotlinPlatform
import app.wesplit.domain.model.currentPlatform
import app.wesplit.domain.model.user.Contact
import app.wesplit.domain.model.user.Plan
import app.wesplit.domain.model.user.PlatformTokens
import app.wesplit.domain.model.user.Setting
import app.wesplit.domain.model.user.User
import app.wesplit.domain.model.user.UserRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.ServerTimestampBehavior
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.messaging.messaging
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single

private const val USER_COLLECTION = "users"

private const val TRX_ID_RECEIVED_EVENT = "trx_id_received_to_plus"

private const val ONBOARDING_STEP_COMPLETED_EVENT = "onboarding_steps_complete"
private const val ONBOARDING_STEP_COMPLETED_EVENT_STEPS_PARAM = "steps"

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

                        is Setting.CompletedOnboardedSteps -> {
                            if (setting.steps.any { it !in authUser.completedOnboardingSteps }) {
                                analytics.track(
                                    event = ONBOARDING_STEP_COMPLETED_EVENT,
                                    params =
                                        mapOf(
                                            ONBOARDING_STEP_COMPLETED_EVENT_STEPS_PARAM to setting.steps.joinToString(),
                                        ),
                                )
                                Firebase.firestore.collection(USER_COLLECTION).document(authUser.id).update(
                                    authUser.copy(
                                        completedOnboardingSteps = authUser.completedOnboardingSteps + setting.steps,
                                    ),
                                )
                            }
                        }

                        is Setting.TransactionId -> {
                            analytics.track(TRX_ID_RECEIVED_EVENT)
                            Firebase.firestore.collection(USER_COLLECTION).document(authUser.id).update(
                                authUser.copy(
                                    transactionId = setting.transactionId,
                                    plan = Plan.PLUS,
                                ),
                            )
                        }
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
        val fcmToken =
            try {
                Firebase.messaging.getToken()
            } catch (e: Throwable) {
                ""
            }

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
                    messagingTokens =
                        PlatformTokens(
                            android = (currentPlatform as? KotlinPlatform.Android)?.let { fcmToken },
                            iOS = (currentPlatform as? KotlinPlatform.Ios)?.let { fcmToken },
                            web = (currentPlatform as? KotlinPlatform.Web)?.let { fcmToken },
                        ),
                )

            Firebase.firestore.collection(USER_COLLECTION).document(authUser.uid).set(User.serializer(), newUser)
        } else {
            val user = doc.data(User.serializer())
            if (!fcmToken.isNullOrBlank()) {
                val messagingTokens =
                    user.messagingTokens.copy(
                        android = (currentPlatform as? KotlinPlatform.Android)?.let { fcmToken },
                        iOS = (currentPlatform as? KotlinPlatform.Ios)?.let { fcmToken },
                        web = (currentPlatform as? KotlinPlatform.Web)?.let { fcmToken },
                    )

                Firebase.firestore.collection(USER_COLLECTION).document(authUser.uid).update(
                    user.copy(
                        messagingTokens = messagingTokens,
                    ),
                )
            }
        }

        return Firebase.firestore.collection(USER_COLLECTION).document(authUser.uid).snapshots.map {
            withContext(coroutineDispatcher) {
                it.data(User.serializer(), ServerTimestampBehavior.ESTIMATE).copy(
                    id = it.id,
                )
            }
        }.catch { e ->
            analytics.log(e)
        }
    }
}
