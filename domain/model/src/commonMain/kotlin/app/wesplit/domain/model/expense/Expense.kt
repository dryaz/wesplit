package app.wesplit.domain.model.expense

import app.wesplit.domain.model.group.Participant
import kotlinx.datetime.Instant

data class Expense(
    val id: String,
    val title: String,
    // TODO: Possible support paying by shares
    val payedBy: Participant,
    val shares: List<Share>,
    val totalAmount: Amount,
    val type: ExpenseType,
    val date: Instant,
    // TODO: Itemization
    // TODO: Comments
)

data class Share(
    val participant: Participant,
    val amount: Amount,
)

fun Expense.myAmount() = shares.find { it.participant.isMe }?.amount ?: Amount(0f, totalAmount.currencyCode)
