package app.wesplit.domain.model.account

import dev.gitlive.firebase.auth.FirebaseUser

sealed interface Account {
    data object Unknown : Account

    data class Anonymous(
        val user: FirebaseUser,
    ) : Account

    data class Authorized(
        val user: FirebaseUser,
    ) : Account
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
