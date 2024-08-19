package app.wesplit.domain.model.account

import dev.gitlive.firebase.auth.FirebaseUser

interface LoginDelegate {
    fun login(
        type: LoginType,
        onLogin: (Result<FirebaseUser>) -> Unit,
    )
}

enum class LoginType {
    GOOGLE,
}
