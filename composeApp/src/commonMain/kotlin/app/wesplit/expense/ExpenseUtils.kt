package app.wesplit.expense

import app.wesplit.domain.model.expense.Amount
import app.wesplit.domain.model.expense.Expense
import app.wesplit.domain.model.expense.Share
import app.wesplit.domain.model.expense.SplitType

internal fun Expense.reCalculateShares(updateAction: UpdateAction.Split? = null): Expense {
    val newSplitType =
        updateAction?.let {
            when (it) {
                is UpdateAction.Split.Amount -> SplitType.AMOUNTS
                is UpdateAction.Split.Equal -> SplitType.EQUAL
                is UpdateAction.Split.Share -> SplitType.SHARES
            }
        } ?: this.splitType

    val newShares =
        when (newSplitType) {
            SplitType.EQUAL -> calculateEqual(totalAmount, shares, updateAction as UpdateAction.Split.Equal?)
            SplitType.SHARES -> calculateShares(totalAmount, shares, updateAction as UpdateAction.Split.Share?)
            SplitType.AMOUNTS -> TODO("Amounts not yet supported")
        }

    return this.copy(
        splitType = newSplitType,
        shares = newShares,
    )
}

fun calculateShares(
    totalAmount: Amount,
    shares: Set<Share>,
    action: UpdateAction.Split.Share?,
): Set<Share> {
    val shareCost = shares.minOf { it.amount.value }
    val participantShares = shares.associate { it.participant to (it.amount.value / shareCost).toFloat() }.toMutableMap()
    action?.let {
        participantShares[it.participant] = it.value
    }

    val newShareCost = totalAmount.value / participantShares.values.sum()
    return participantShares.map { Share(it.key, Amount((newShareCost * it.value), totalAmount.currencyCode)) }.toSet()
}

private fun calculateEqual(
    totalAmountValue: Amount,
    shares: Set<Share>,
    action: UpdateAction.Split.Equal?,
): Set<Share> {
    val currentParticipants = shares.map { it.participant }.toSet()
    val newParticipants =
        if (action != null) {
            if (action.isIncluded) {
                currentParticipants + action.participant
            } else {
                currentParticipants - action.participant
            }
        } else {
            currentParticipants
        }

    val shareCost = totalAmountValue.value / newParticipants.size
    return newParticipants.map { Share(it, Amount(shareCost, totalAmountValue.currencyCode)) }.toSet()
}
