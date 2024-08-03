package app.wesplit.domain.model.account

data class Account(
    val id: String,
    val name: String,
    val contacts: List<Contact> = emptyList()
)

sealed interface Contact {
    class Email(val email: String): Contact
    class Phone(val phone: String): Contact
    class Telegram(val account: String): Contact
}