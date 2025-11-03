package app.wesplit.domain.model.account

import dev.gitlive.firebase.auth.FirebaseUser

interface LoginDelegate {
    fun socialLogin(
        type: Login.Social.Type,
        onLogin: (Result<FirebaseUser>) -> Unit,
    )

    fun anonymousLogin(onLogin: (Result<FirebaseUser>) -> Unit)
}

sealed interface Login {
    data class Social(val type: Type) : Login {
        enum class Type {
            GOOGLE,
            APPLE,
        }
    }

    data class GroupToken(val groupId: String, val token: String) : Login

    data object Anonymous : Login
}
