package app.wesplit.domain.model.account

sealed interface Account {
    data object Unknown : Account

    data object Unregistered : Account

    data class Authorized(
        val id: String,
        val name: String,
        val contacts: List<Contact> = emptyList(),
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
