package app.wesplit.data.firebase

import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.account.Account
import app.wesplit.domain.model.account.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Single

private const val LOGIN_EVENT = "login"

@Single
class AccountFirebaseRepository(
    private val analyticsManager: AnalyticsManager,
) : AccountRepository {
    private val accountState = MutableStateFlow<Account>(Account.Unregistered)

    override fun get(): StateFlow<Account> = accountState

    override fun update(account: Account) {
        TODO("Not yet implemented")
    }
}
