package app.wesplit.domain.model.account

interface AccountRepository {
    suspend fun get(): Account
}
