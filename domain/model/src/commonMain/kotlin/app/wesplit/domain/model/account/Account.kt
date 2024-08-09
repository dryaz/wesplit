package app.wesplit.domain.model.account

import app.wesplit.domain.model.user.User

sealed interface Account {
    data object Unknown : Account

    data object Unregistered : Account

    data class Authorized(
        val user: User,
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
