package app.wesplit.domain.model.account

import app.wesplit.domain.model.user.User
import app.wesplit.domain.model.user.isPlus
import app.wesplit.domain.model.user.participant
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.StateFlow

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
        val user: StateFlow<User?>,
    ) : Account
}

fun Account.isPlus() = (this as? Account.Authorized)?.user?.value?.isPlus() == true

fun Account.participant() = (this as? Account.Authorized)?.user?.value?.participant()
