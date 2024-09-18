package app.wesplit.domain.model.expense

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("splitType")
enum class SplitType {
    @SerialName("equal")
    EQUAL,

    @SerialName("shares")
    SHARES,

    @SerialName("amounts")
    AMOUNTS,
}
