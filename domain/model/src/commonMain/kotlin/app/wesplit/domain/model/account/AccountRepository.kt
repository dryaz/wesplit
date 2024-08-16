package app.wesplit.domain.model.account

import kotlinx.coroutines.flow.StateFlow

interface AccountRepository {
    fun get(): StateFlow<Account>

    fun update(account: Account)
}
