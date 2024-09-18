package app.wesplit.domain.model.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * User is an authorized user info.
 * If somebody creates groups with anonymous 'user' it treated as participant.
 */

@Serializable
@SerialName("user")
data class User(
    @Transient
    val id: String = "",
    @SerialName("name")
    val name: String,
    // TODO: Ux improvement - if photoUrl is null generate colorful template based on hash of id/name
    @SerialName("photo")
    val photoUrl: String? = null,
    @SerialName("contacts")
    val contacts: List<Contact> = emptyList(),
    @SerialName("authIds")
    val authIds: List<String> = emptyList(),
)

fun User.email() = (contacts.find { it is Contact.Email } as? Contact.Email)?.email

@Serializable
sealed interface Contact {
    @Serializable
    @SerialName("email")
    data class Email(
        @SerialName("email")
        val email: String,
    ) : Contact

    @Serializable
    @SerialName("phone")
    data class Phone(
        @SerialName("phone")
        val phone: String,
    ) : Contact

    @Serializable
    @SerialName("telegram")
    data class Telegram(
        @SerialName("account")
        val account: String,
    ) : Contact
}
