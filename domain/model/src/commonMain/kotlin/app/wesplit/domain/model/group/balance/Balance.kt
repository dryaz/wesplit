package app.wesplit.domain.model.group.balance

import app.wesplit.domain.model.expense.Amount
import app.wesplit.domain.model.group.Participant

data class Balance(
    val participants: Map<Participant, ParticipantStat>,
    val nonDistributed: Amount,
)

data class ParticipantStat(
    val balance: Amount,
)
