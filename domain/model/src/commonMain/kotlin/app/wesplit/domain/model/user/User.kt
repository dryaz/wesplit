package app.wesplit.domain.model.user

import app.wesplit.domain.model.group.Participant
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * User is an authorized user info.
 * If somebody creates groups with anonymous 'user' it treated as participant.
 */

@OptIn(ExperimentalSerializationApi::class)
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
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val contacts: List<Contact> = emptyList(),
    @SerialName("authIds")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val authIds: List<String> = emptyList(),
    @SerialName("lastCur")
    val lastUsedCurrency: String? = null,
    @SerialName("subs")
    val plan: Plan = Plan.BASIC,
    @SerialName("onboard")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val completedOnboardingSteps: List<OnboardingStep> = emptyList(),
)

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

@Serializable
@SerialName("subs")
enum class Plan {
    @SerialName("basic")
    BASIC,

    @SerialName("plus")
    PLUS,
}

@Serializable
@SerialName("step")
enum class OnboardingStep {
    @SerialName("ga")
    GROUP_ADD,

    @SerialName("anu")
    ADD_NEW_USER_BUTTON,

    @SerialName("tpn")
    TYPE_PARTICIPANT_NAME,

    @SerialName("cnu")
    CREATE_NEW_USER_IN_GROUP,

    @SerialName("sg")
    SAVE_GROUP,

    @SerialName("ach")
    APPLY_CHANGES,

    @SerialName("ea")
    EXPENSE_ADD,

    @SerialName("bc")
    BALANCE_CHOOSER,

    @SerialName("bp")
    BALANCE_PREVIEW,

    @SerialName("su")
    SETTLE_UP,
}

fun User.participant(): Participant =
    Participant(
        name = name,
        user = this,
    )

fun User.isPlus() = plan == Plan.PLUS

fun User.email() = (contacts.find { it is Contact.Email } as? Contact.Email)?.email
