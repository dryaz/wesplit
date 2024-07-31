package app.wesplit.data.firebase

import app.wesplit.domain.model.UserRepository
import org.koin.core.annotation.Single

@Single
class UserFirebaseRepository: UserRepository {
    override fun get(): String = "Hooray !"
}