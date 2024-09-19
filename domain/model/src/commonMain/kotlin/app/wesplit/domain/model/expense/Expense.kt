package app.wesplit.domain.model.expense

import app.wesplit.domain.model.group.Participant
import app.wesplit.domain.model.group.isMe
import dev.gitlive.firebase.firestore.BaseTimestamp
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.datetime.Instant
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@SerialName("expense")
data class Expense(
    @Transient
    val id: String? = null,
    @SerialName("title")
    val title: String,
    // TODO: Possible support paying by shares
    @SerialName("payedBy")
    val payedBy: Participant,
    @SerialName("shares")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val shares: Set<Share> = emptySet(),
    @SerialName("totalAmount")
    val totalAmount: Amount,
    @SerialName("undistributedAmount")
    val undistributedAmount: Amount?,
    @SerialName("expenseType")
    val expenseType: ExpenseType,
    @SerialName("date")
    val date: BaseTimestamp = Timestamp.ServerTimestamp,
    // TODO: Yet support only equal split in v1
    @SerialName("splitType")
    val splitType: SplitType = SplitType.EQUAL,
    // TODO: Itemization
    // TODO: Comments
)

fun BaseTimestamp.toInstant() = Instant.fromEpochSeconds((this as Timestamp).seconds)

@Serializable
@SerialName("share")
data class Share(
    val participant: Participant,
    val amount: Amount,
)

fun Expense.myAmount() = shares.find { it.participant.isMe() }?.amount ?: Amount(0f, totalAmount.currencyCode)
