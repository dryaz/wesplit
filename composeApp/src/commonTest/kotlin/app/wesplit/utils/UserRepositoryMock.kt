package app.wesplit.utils

import app.wesplit.domain.model.user.Setting
import app.wesplit.domain.model.user.User
import app.wesplit.domain.model.user.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserRepositoryMock(
    private val user: User? = null,
) : UserRepository {
    override fun get(): StateFlow<User?> = MutableStateFlow(user)

    override fun update(setting: Setting) {
        TODO("Not yet implemented")
    }

    override suspend fun delete() {
        TODO("Not yet implemented")
    }
}
