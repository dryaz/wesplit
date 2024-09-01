package app.wesplit.domain.model.user

/**
 * User is an authorized user info.
 * If somebody creates groups with anonymous 'user' it treated as participant.
 */
data class User(
    val id: String,
    val name: String,
    // TODO: Ux improvement - if photoUrl is null generate colorful template based on hash of id/name
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
