package app.wesplit.domain.model.expense

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("status")
enum class ExpenseStatus {
    @SerialName("NEW")
    NEW,

    @SerialName("SETTLED")
    SETTLED,
}
