package app.wesplit.domain.model.expense

import app.wesplit.domain.model.account.Account
import app.wesplit.domain.model.group.Group

data class Expense(
    val id: String,
    val group: Group,
    val participants: List<Participant>,
    val title: String,
    val amount: Amount,
    val splitType: SplitType,
    val type: ExpenseType
    // TODO: Itemization
    // TODO: Comments
)

data class Participant(val account: Account, val amount: Amount)
