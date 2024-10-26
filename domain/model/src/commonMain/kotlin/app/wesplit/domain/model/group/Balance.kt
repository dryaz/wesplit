package app.wesplit.domain.model.group

import app.wesplit.domain.model.currency.Amount
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@SerialName("balance")
data class Balance(
    @SerialName("participantsBalance")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val participantsBalance: Set<ParticipantBalance> = emptySet(),
    @SerialName("undistributed")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val undistributed: Set<Amount> = emptySet(),
    @SerialName("invalid")
    val invalid: Boolean = false,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@SerialName("pBalance")
data class ParticipantBalance(
    @SerialName("participant")
    val participant: Participant,
    @SerialName("amounts")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val amounts: Set<Amount> = emptySet(),
)
