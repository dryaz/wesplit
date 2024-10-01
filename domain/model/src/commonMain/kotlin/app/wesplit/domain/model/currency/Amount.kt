package app.wesplit.domain.model.currency

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
@SerialName("amount")
data class Amount(
    @SerialName("value")
    val value: Double,
    @SerialName("currency")
    val currencyCode: String,
)

fun Amount.format(withCurrency: Boolean = true) =
    "${if (withCurrency) currencyCode.currencySymbol() else ""}${(value * 100.0).roundToInt() / 100.0}"

fun String.currencySymbol() = currencySymbols.get(this) ?: this
