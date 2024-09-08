package app.wesplit.domain.model.expense

import kotlin.math.roundToInt

data class Amount(
    val value: Float,
    val currencyCode: String,
)

// TODO: KMP amount formatting
fun Amount.format() = "$currencyCode ${(value * 100f).roundToInt() / 100f}"
