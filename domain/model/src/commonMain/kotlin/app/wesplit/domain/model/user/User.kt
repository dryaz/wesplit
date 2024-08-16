package app.wesplit.domain.model.user

data class User(
    val id: String,
    val name: String,
    val photoUrl: String?,
    val contacts: List<Contact> = emptyList(),
)

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
