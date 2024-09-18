package app.wesplit.domain.model.group.balance

import app.wesplit.domain.model.expense.Amount
import app.wesplit.domain.model.group.Participant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("balance")
data class Balance(
    @SerialName("participants")
    val participants: Map<Participant, ParticipantStat>,
    @SerialName("nonDistrAmount")
    val nonDistributed: Amount,
)

@Serializable
@SerialName("stat")
data class ParticipantStat(
    @SerialName("balance")
    val balance: Amount,
)
