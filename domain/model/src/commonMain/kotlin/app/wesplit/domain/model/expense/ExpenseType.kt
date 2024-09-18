package app.wesplit.domain.model.expense

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("expenseType")
enum class ExpenseType {
    @SerialName("expense")
    EXPENSE,

    @SerialName("settlement")
    SETTLEMENT,
}
