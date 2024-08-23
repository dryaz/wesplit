package app.wesplit.domain.model.user

import app.wesplit.domain.model.account.Account

data class User(
    val id: String,
    val name: String,
    val photoUrl: String?,
    val contacts: List<Contact> = emptyList(),
    val isCurrentUser: Boolean = false,
)

fun Account.user() =
    when (this) {
        is Account.Anonymous -> user
        is Account.Authorized -> user
        Account.Unknown -> null
    }?.let { firebaseUser ->
        User(
            id = firebaseUser.uid,
            name = firebaseUser.displayName ?: firebaseUser.email ?: firebaseUser.phoneNumber ?: firebaseUser.uid,
            photoUrl = firebaseUser.photoURL,
            isCurrentUser = true,
        )
    }

sealed interface Contact {
    class Email(
        val email: String,
    ) : Contact

    class Phone(
        val phone: String,
    ) : Contact

    class Telegram(
        val account: String,
    ) : Contact
}
