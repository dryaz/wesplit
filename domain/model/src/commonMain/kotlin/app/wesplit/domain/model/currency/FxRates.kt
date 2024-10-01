package app.wesplit.domain.model.currency

import dev.gitlive.firebase.firestore.BaseTimestamp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("fxrates")
data class FxRates(
    @SerialName("base")
    val base: String,
    @SerialName("rates")
    val rates: Map<String, Double>,
    @SerialName("lastUpdate")
    val updatedAt: BaseTimestamp? = null,
)
