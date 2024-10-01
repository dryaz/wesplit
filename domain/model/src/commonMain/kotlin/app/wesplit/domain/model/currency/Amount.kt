package app.wesplit.domain.model.currency

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.roundToInt

@Serializable
@SerialName("amount")
data class Amount(
    @SerialName("value")
    val value: Double,
    @SerialName("currency")
    val currencyCode: String,
)

fun Amount.format(withCurrency: Boolean = true): String {
    val builder = StringBuilder()
    if (value < 0.0) builder.append('-')
    if (withCurrency) builder.append(currencyCode.currencySymbol())
    builder.append(abs((value * 100.0).roundToInt() / 100.0))
    return builder.toString()
}

fun String.currencySymbol() = currencySymbols.get(this) ?: this
