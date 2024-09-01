package app.wesplit.data.firebase

import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.account.Account
import app.wesplit.domain.model.account.AccountRepository
import app.wesplit.domain.model.account.LoginDelegate
import app.wesplit.domain.model.account.LoginType
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

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

    private val authListener =
        Firebase.auth.authStateChanged.map { user ->
            val account = getAccount(user)
            // TODO: Enabled anon here creates a lot of users AND leave user images empty.
            //  Maybe need to create user just when user want to create group/expense, TBD.
            account
        }.stateIn(
            scope = coroutinScope,
            started = SharingStarted.Lazily,
            initialValue = Account.Unknown,
        )

    override fun getCurrent(): Account = getAccount(Firebase.auth.currentUser)

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

    private fun getAccount(user: FirebaseUser?) =
        if (user == null) {
            Account.Unknown
        } else {
            if (user.isAnonymous) {
                Account.Anonymous
            } else {
                Account.Authorized(user)
            }
        }
}
