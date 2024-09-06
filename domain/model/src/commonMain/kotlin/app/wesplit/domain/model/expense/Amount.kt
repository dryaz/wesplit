package app.wesplit.domain.model.expense

data class Amount(
    val amount: Float,
    val currencyCode: String,
)

// TODO: KMP amount formatting
fun Amount.format() = "$currencyCode $amount"
