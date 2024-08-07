package app.wesplit.data.firebase

import app.wesplit.domain.model.account.Account
import app.wesplit.domain.model.account.AccountRepository
import org.koin.core.annotation.Single

@Single
class AccountFirebaseRepository : AccountRepository {
    override suspend fun get(): Account = Account.Unknown
}
