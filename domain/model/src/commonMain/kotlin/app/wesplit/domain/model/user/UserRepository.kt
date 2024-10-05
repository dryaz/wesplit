package app.wesplit.domain.model.user

import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
    fun get(): StateFlow<User?>

    fun update(setting: Setting)

    suspend fun delete()
}

sealed interface Setting {
    data class Currency(val code: String) : Setting
}
