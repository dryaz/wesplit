package app.wesplit.domain.model.account

import kotlinx.coroutines.flow.StateFlow

interface AccountRepository {
    fun getCurrent(): Account

    fun get(): StateFlow<Account>

    fun logout()

    fun login(loginType: LoginType)
}
