package app.wesplit.domain.model.account

import kotlinx.coroutines.flow.StateFlow

interface AccountRepository {
    fun get(): StateFlow<Account>

    fun logout()

    fun login(login: Login)

    fun deleteAccount()
}
