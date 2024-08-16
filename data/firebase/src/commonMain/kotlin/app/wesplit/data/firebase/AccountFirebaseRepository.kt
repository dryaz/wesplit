package app.wesplit.data.firebase

import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.account.Account
import app.wesplit.domain.model.account.AccountRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.Single

private const val LOGIN_EVENT = "login"

@Single
class AccountFirebaseRepository(
    private val analyticsManager: AnalyticsManager,
) : AccountRepository {
    private val accountState = MutableStateFlow<Account>(Account.Unregistered)
    private val mainScope = CoroutineScope(Dispatchers.Main)

    private val authListener =
        Firebase.auth.authStateChanged.map { user ->
            if (user == null) {
                // TODO: Anonymous user as well
                Account.Unregistered
            } else {
                Account.Authorized(user)
            }
        }.stateIn(
            scope = mainScope,
            started = SharingStarted.Lazily,
            initialValue = Account.Unknown,
        )

    override fun get(): StateFlow<Account> = authListener
}
