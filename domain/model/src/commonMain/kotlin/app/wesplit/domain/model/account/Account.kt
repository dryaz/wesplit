package app.wesplit.domain.model.account

import app.wesplit.domain.model.group.Participant
import app.wesplit.domain.model.user.User
import dev.gitlive.firebase.auth.FirebaseUser

sealed interface Account {
    data object Unknown : Account

    data object Anonymous : Account

    data class Authorized(
        val user: User,
        val authUser: FirebaseUser,
    ) : Account
}

fun Account.participant(): Participant? =
    (this as? Account.Authorized)?.user?.let { user ->
        Participant(
            name = user.name,
            user = user,
        )
    }
