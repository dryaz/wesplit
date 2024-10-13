package app.wesplit.domain.model.account

import app.wesplit.domain.model.group.Participant
import app.wesplit.domain.model.user.Subscription
import app.wesplit.domain.model.user.User
import dev.gitlive.firebase.auth.FirebaseUser

sealed interface Account {
    data object Unknown : Account

    data object Anonymous : Account

    /**
     * Not yet full account but restircted to explicit entities.
     * E.g. Acces to the group by public token.
     */
    data object Restricted : Account

    data class Authorized(
        val authUser: FirebaseUser,
        val user: User,
    ) : Account
}

fun Account.participant(): Participant? =
    (this as? Account.Authorized)?.user?.let { user ->
        Participant(
            name = user.name,
            user = user,
        )
    }

fun Account.isPlus() = (this as? Account.Authorized)?.user?.subscription == Subscription.PLUS
