package app.wesplit.domain.model.group.balance

import app.wesplit.domain.model.expense.Amount
import app.wesplit.domain.model.group.Participant
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@SerialName("balance")
data class Balance(
    @SerialName("participants")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val participants: Map<Participant, ParticipantStat> = emptyMap(),
    @SerialName("nonDistrAmount")
    val nonDistributed: Amount,
)

@Serializable
@SerialName("stat")
data class ParticipantStat(
    @SerialName("balance")
    val balance: Amount,
)
