package app.wesplit.data.firebase

import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.account.Account
import app.wesplit.domain.model.account.AccountRepository
import app.wesplit.domain.model.account.LoginDelegate
import app.wesplit.domain.model.account.LoginType
import app.wesplit.domain.model.user.Contact
import app.wesplit.domain.model.user.User
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

private const val USER_COLLECTION = "users"

private const val LOGIN_ATTEMPT_EVENT = "login_attempt"
private const val LOGIN_SUCCEED_EVENT = "login"
private const val LOGIN_FAILED_EVENT = "login_failed"

private const val LOGIN_PROVIDER_PARAM = "provider"

@Single
class AccountFirebaseRepository(
    private val analytics: AnalyticsManager,
    private val coroutineDispatcher: CoroutineDispatcher,
    private val loginDelegate: LoginDelegate,
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

    override fun login(loginType: LoginType) {
        val providerParam = mapOf(LOGIN_PROVIDER_PARAM to loginType.toString())
        analytics.track(LOGIN_ATTEMPT_EVENT, providerParam)
        loginDelegate.login(LoginType.GOOGLE) { result ->
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

    private suspend fun getAccount(authUser: FirebaseUser?): Account {
        if (authUser == null || authUser.isAnonymous) {
            return Account.Anonymous
        }

        val doc = Firebase.firestore.collection(USER_COLLECTION).document(authUser.uid).get()
        if (doc.exists) {
            val user = doc.data(User.serializer())
            return Account.Authorized(
                user = user.copy(id = doc.id),
                authUser = authUser,
            )
        } else {
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
            return Account.Authorized(
                user = newUser.copy(id = authUser.uid),
                authUser = authUser,
            )
        }
    }
}
