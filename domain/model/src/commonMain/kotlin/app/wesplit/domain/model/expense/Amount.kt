package app.wesplit.domain.model.expense

import kotlin.math.roundToInt

data class Amount(
    val amount: Float,
    val currencyCode: String,
)

// TODO: KMP amount formatting
fun Amount.format() = "$currencyCode ${(amount * 100f).roundToInt() / 100f}"
