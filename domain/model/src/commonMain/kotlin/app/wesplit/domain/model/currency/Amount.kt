package app.wesplit.domain.model.currency

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.roundToLong

@Serializable
@SerialName("amount")
data class Amount(
    @SerialName("value")
    val value: Double,
    @SerialName("currency")
    val currencyCode: String,
)

fun Amount.format(withCurrency: Boolean = true): String {
    val isNegative = value < 0
    val absValue = abs(value)

    // Round to two decimal places using Long to prevent overflow
    val rounded = (absValue * 100.0).roundToLong() / 100.0

    // Split integer and decimal parts
    val integerPart = rounded.toLong()
    val decimalPart = ((rounded - integerPart) * 100).toInt()

    // Format integer part with thousand separators (dots)
    val integerStr = integerPart.toString().reversed().chunked(3).joinToString(".").reversed()

    // Format decimal part with two digits
    val decimalStr = decimalPart.toString().padStart(2, '0')

    // Combine integer and decimal parts with comma as decimal separator
    val formattedNumber = "$integerStr,$decimalStr"

    // Build the final formatted string
    val builder = StringBuilder()
    if (isNegative) builder.append('-')
    if (withCurrency) builder.append(currencyCode.currencySymbol())
    builder.append(formattedNumber)

    return builder.toString()
}

fun String.currencySymbol() = currencySymbols.get(this) ?: this
