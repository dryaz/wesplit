package app.wesplit.domain.model.user

import app.wesplit.domain.model.group.Participant
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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
    val plan: Plan? = Plan.BASIC,
    @SerialName("expiresAt")
    val planExpiration: Timestamp? = null,
    @SerialName("onboard")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val completedOnboardingSteps: List<OnboardingStep> = emptyList(),
    @SerialName("trxId")
    val transactionId: String? = null,
    @SerialName("fcm")
    val messagingTokens: Set<String> = emptySet(),
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

    @SerialName("shg")
    SHARE_GROUP,

    @SerialName("et")
    EXPENSE_TITLE,

    @SerialName("eam")
    EXPENSE_AMOUNT,

    @SerialName("edt")
    EXPENSE_DATE_CURRENCY,

    @SerialName("ep")
    EXPENSE_PAYER,

    @SerialName("esp")
    EXPENSE_SPLIT,

    @SerialName("esa")
    EXPENSE_SAVE,
}

@OptIn(ExperimentalUuidApi::class)
fun User.participant(): Participant =
    Participant(
        id = if (id.isNullOrBlank()) Uuid.random().toString() else id,
        name = name,
        user = this,
    )

fun User?.isPlus() = this?.let { plan == Plan.PLUS && !isSubscriptionExpired() } ?: false

fun User?.isAuthorized() = this?.let { !authIds.isNullOrEmpty() } ?: false

fun User.email() = (contacts.find { it is Contact.Email } as? Contact.Email)?.email

fun User.planValidTill() = planExpiration?.let { Instant.fromEpochSeconds(it.seconds, it.nanoseconds.toLong()) }

// Function to compare planExpiration with current time
private fun User.isSubscriptionExpired(): Boolean {
    // Check if planExpiration is not null
    if (planExpiration != null) {
        // Convert Firestore Timestamp to kotlinx.datetime.Instant
        val expirationInstant: Instant = Instant.fromEpochSeconds(planExpiration.seconds, planExpiration.nanoseconds.toLong())
        // Get the current instant
        val currentInstant: Instant = Clock.System.now()

        // Compare the two Instants
        return expirationInstant < currentInstant
    }

    // If planExpiration is null, decide on your logic (e.g., not expired)
    return false
}
