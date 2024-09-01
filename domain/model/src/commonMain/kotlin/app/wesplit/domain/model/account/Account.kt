package app.wesplit.domain.model.account

import app.wesplit.domain.model.group.Participant
import app.wesplit.domain.model.user.User
import dev.gitlive.firebase.auth.FirebaseUser

sealed interface Account {
    data object Unknown : Account

    data object Anonymous : Account

    data class Authorized(
        val user: FirebaseUser,
    ) : Account
}

fun Account.participant(): Participant? =
    (this as? Account.Authorized)?.user?.let { authUser ->
        Participant(
            id = authUser.uid,
            name = authUser.displayName ?: authUser.email ?: authUser.phoneNumber ?: authUser.uid,
            user = this.user(),
            isMe = true,
        )
    }

private fun Account.user(): User? =
    (this as? Account.Authorized)?.user?.let { authUser ->
        User(
            id = authUser.uid,
            name = authUser.displayName ?: authUser.email ?: authUser.phoneNumber ?: authUser.uid,
            photoUrl = authUser.photoURL,
        )
    }
