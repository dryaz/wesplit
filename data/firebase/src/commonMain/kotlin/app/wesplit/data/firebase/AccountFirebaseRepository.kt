package app.wesplit.data.firebase

import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.account.Account
import app.wesplit.domain.model.account.AccountRepository
import app.wesplit.domain.model.account.Login
import app.wesplit.domain.model.account.LoginDelegate
import app.wesplit.domain.model.user.UserRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.functions.functions
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

private const val LOGIN_ATTEMPT_EVENT = "login_attempt"
private const val LOGIN_SUCCEED_EVENT = "login"
private const val LOGIN_FAILED_EVENT = "login_failed"

private const val LOGIN_PROVIDER_PARAM = "provider"
private const val LOGIN_GROUP_ID = "group_id"

@Single
class AccountFirebaseRepository(
    private val analytics: AnalyticsManager,
    private val coroutineDispatcher: CoroutineDispatcher,
    private val loginDelegate: LoginDelegate,
    private val userRepository: UserRepository,
) : AccountRepository {
    private val accountState = MutableStateFlow<Account>(Account.Unknown)
    private val coroutinScope = CoroutineScope(coroutineDispatcher)

    private val authListener: StateFlow<Account> =
        Firebase.auth.authStateChanged.map { user ->
            getAccount(user)
        }.stateIn(
            scope = coroutinScope,
            started = SharingStarted.Lazily,
            initialValue = Account.Unknown,
        )

    override fun get(): StateFlow<Account> = authListener

    override fun logout() {
        coroutinScope.launch {
            Firebase.auth.signOut()
        }
    }

    override fun deleteAccount() {
        val authUser = Firebase.auth.currentUser
        authUser?.let {
            coroutinScope.launch {
                userRepository.delete()
                Firebase.auth.currentUser?.delete()
            }
        }
    }

    override fun login(login: Login) {
        when (login) {
            is Login.GroupToken ->
                coroutinScope.launch {
                    try {
                        signInWithPublicToken(login.groupId, login.token)
                    } catch (e: Exception) {
                        analytics.log(e)
                    }
                }

            is Login.Social -> {
                val providerParam = mapOf(LOGIN_PROVIDER_PARAM to login.type.toString())
                analytics.track(LOGIN_ATTEMPT_EVENT, providerParam)
                loginDelegate.socialLogin(login.type) { result ->
                    if (result.isSuccess) {
                        analytics.track(LOGIN_SUCCEED_EVENT, providerParam)
                    } else {
                        analytics.track(LOGIN_FAILED_EVENT, providerParam)
                        result.exceptionOrNull()?.let {
                            analytics.log(it)
                        }
                    }
                }
            }

            Login.Anonymous ->
                coroutinScope.launch {
                    try {
                        Firebase.auth.signInAnonymously()
                    } catch (e: Exception) {
                        analytics.log(e)
                    }
                }
        }
    }

    private suspend fun signInWithPublicToken(
        groupId: String,
        publicToken: String,
    ) {
        val providerParam =
            mapOf(
                LOGIN_PROVIDER_PARAM to "group_token",
                LOGIN_GROUP_ID to groupId,
            )
        analytics.track(LOGIN_ATTEMPT_EVENT, providerParam)
        // Get an instance of Firebase Functions
        val functions = Firebase.functions

        // Get a reference to the 'generateGroupToken' callable function
        val generateGroupToken = functions.httpsCallable("generateGroupToken")

        try {
            // Call the Cloud Function with the required data
            val result = generateGroupToken.invoke(mapOf("groupId" to groupId, "publicToken" to publicToken))

            // Extract the custom token from the result
            val data = result.data() as Map<String, String>
            val customToken = data["customToken"] as String

            // Sign in with the custom token using Firebase Auth
            Firebase.auth.signInWithCustomToken(customToken)

            analytics.track(LOGIN_SUCCEED_EVENT, providerParam)
        } catch (e: Exception) {
            analytics.track(LOGIN_FAILED_EVENT, providerParam)
            analytics.log(e)
        }
    }

    private suspend fun getAccount(authUser: FirebaseUser?): Account {
        if (authUser == null) {
            return Account.Anonymous
        }

        if (authUser.uid.startsWith("group")) {
            return Account.Restricted
        }

        val user = userRepository.get().filterNotNull().first()
        return Account.Authorized(authUser, user)
    }
}
