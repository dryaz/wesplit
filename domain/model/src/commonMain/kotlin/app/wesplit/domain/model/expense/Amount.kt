package app.wesplit.domain.model.expense

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
@SerialName("amount")
data class Amount(
    @SerialName("value")
    val value: Float,
    @SerialName("currency")
    val currencyCode: String,
)

// TODO: KMP amount formatting
fun Amount.format(withCurrency: Boolean = true) = "${if (withCurrency) "$" else ""}${(value * 100f).roundToInt() / 100f}"
