package app.wesplit.expense

import app.wesplit.domain.model.currency.Amount
import app.wesplit.domain.model.expense.Expense
import app.wesplit.domain.model.expense.Share
import app.wesplit.domain.model.expense.SplitType
import app.wesplit.domain.model.group.Participant

internal fun Expense.getInitialSplitOptions(extraParticipants: Collection<Participant> = emptySet()) =
    ExpenseDetailsViewModel.State.Data.SplitOptions(
        selectedSplitType = if (shares.distinctBy { it.amount.value }.size == 1) SplitType.EQUAL else SplitType.AMOUNTS,
        splitValues =
            SplitType.entries.map {
                it to
                    when (it) {
                        SplitType.EQUAL ->
                            (
                                shares
                                    .map { it.participant to (it.amount.value != 0.0 || totalAmount.value == 0.0) } +
                                    extraParticipants.map { it to false }
                            ).toMap()

                        SplitType.SHARES ->
                            (
                                shares.map {
                                    val shareCost = shares.getMinShareCost()
                                    it.participant to (if (shareCost != 0.0) it.amount.value / shareCost else 1.0)
                                } + extraParticipants.map { it to 0.0 }
                            ).toMap()

                        SplitType.AMOUNTS ->
                            (
                                shares.map { it.participant to it.amount.value } + extraParticipants.map { it to 0.0 }
                            ).toMap()
                    }
            }.toMap(),
    )

internal fun ExpenseDetailsViewModel.State.Data.SplitOptions.update(
    action: UpdateAction.TotalAmount,
): ExpenseDetailsViewModel.State.Data.SplitOptions {
    val shareValues = splitValues[SplitType.SHARES]
    val totalShares = shareValues?.values?.sumOf { (it as Double) } ?: 0.0
    val shareCost = (if (totalShares != 0.0) action.value / totalShares else 0.0)

    val updatedOptions =
        copy(
            splitValues =
                splitValues.mapValues { entry ->
                    if (entry.key != SplitType.AMOUNTS) {
                        entry.value
                    } else {
                        entry.value.mapValues { entry ->
                            (shareValues?.get(entry.key) as Double? ?: 0.0) * shareCost
                        }
                    }
                },
        )

    return updatedOptions
}

internal fun ExpenseDetailsViewModel.State.Data.SplitOptions.update(
    action: UpdateAction.Split,
): ExpenseDetailsViewModel.State.Data.SplitOptions {
    val newSplitType =
        when (action) {
            is UpdateAction.Split.Amount -> SplitType.AMOUNTS
            is UpdateAction.Split.Equal -> SplitType.EQUAL
            is UpdateAction.Split.Share -> SplitType.SHARES
        }

    val newSplitValueForAction =
        splitValues[newSplitType]!!.mapValues { entry ->
            if (entry.key == action.participant) action.value else entry.value
        }

    return copy(
        selectedSplitType = newSplitType,
        splitValues =
            splitValues.mapValues { entry ->
                calculateForSplitType(entry.key, entry.value, newSplitType, newSplitValueForAction)
            },
    )
}

private fun calculateForSplitType(
    splitType: SplitType,
    oldValue: Map<Participant, Any>,
    baseSplitType: SplitType,
    baseValue: Map<Participant, Any>,
): Map<Participant, Any> {
    if (splitType == baseSplitType) return baseValue

    return when (baseSplitType) {
        SplitType.EQUAL -> {
            when (splitType) {
                SplitType.SHARES -> baseValue.mapValues { if ((it.value as Boolean) == true) 1.0 else 0.0 }
                SplitType.AMOUNTS -> {
                    val total = oldValue.values.sumOf { (it as Double) }
                    val participants = baseValue.values.count { (it as Boolean) == true }
                    val share = (if (participants != 0) total / participants else 0.0)
                    baseValue.mapValues { if ((it.value as Boolean) == true) share else 0.0 }
                }

                SplitType.EQUAL -> throw IllegalStateException("Calculate for same split type: $splitType")
            }
        }

        SplitType.SHARES -> {
            when (splitType) {
                SplitType.AMOUNTS -> {
                    val total = oldValue.values.sumOf { (it as Double) }
                    val shares = baseValue.values.sumOf { (it as Double) }
                    val sharePrice = total / shares
                    baseValue.mapValues { (it.value as Double) * sharePrice }
                }

                SplitType.EQUAL -> baseValue.mapValues { true }
                SplitType.SHARES -> throw IllegalStateException("Calculate for same split type: $splitType")
            }
        }

        SplitType.AMOUNTS -> {
            when (splitType) {
                SplitType.SHARES -> {
                    val sharePrice = baseValue.values.filter { (it as Double) != 0.0 }.minOfOrNull { it as Double } ?: 0.0
                    baseValue.mapValues { (it.value as Double) / sharePrice }
                }

                SplitType.EQUAL -> baseValue.mapValues { true }
                SplitType.AMOUNTS -> throw IllegalStateException("Calculate for same split type: $splitType")
            }
        }
    }
}

internal fun Expense.reCalculateShares(splitOptions: ExpenseDetailsViewModel.State.Data.SplitOptions): Expense {
    val shareValues =
        splitOptions.splitValues[splitOptions.selectedSplitType]
            ?: throw IllegalStateException("Cant get split options for ${splitOptions.selectedSplitType}")

    return when (splitOptions.selectedSplitType) {
        SplitType.EQUAL -> {
            val participantCount = shareValues.count { (it.value as Boolean) == true }
            val sharePrice = if (participantCount != 0) totalAmount.value / participantCount else 0.0
            copy(
                shares =
                    shareValues.map { entry ->
                        Share(
                            participant = entry.key,
                            amount =
                                Amount(
                                    value = if (entry.value as Boolean) sharePrice else 0.0,
                                    currencyCode = totalAmount.currencyCode,
                                ),
                        )
                    }.toSet(),
                undistributedAmount = if (participantCount == 0) totalAmount else null,
            )
        }

        SplitType.SHARES -> {
            val totalShares = shareValues.values.sumOf { (it as Double) }
            val sharePrice = if (totalShares != 0.0) (totalAmount.value / totalShares) else 0.0
            copy(
                shares =
                    shareValues.map { entry ->
                        Share(
                            participant = entry.key,
                            amount =
                                Amount(
                                    value = (entry.value as Double) * sharePrice,
                                    currencyCode = totalAmount.currencyCode,
                                ),
                        )
                    }.toSet(),
                undistributedAmount = if (totalShares == 0.0) totalAmount else null,
            )
        }

        SplitType.AMOUNTS -> {
            val distributed = shareValues.values.sumOf { it as Double }
            val undistributed = totalAmount.value - distributed
            copy(
                shares =
                    shareValues.map { entry ->
                        Share(
                            participant = entry.key,
                            amount =
                                Amount(
                                    value = entry.value as Double,
                                    currencyCode = totalAmount.currencyCode,
                                ),
                        )
                    }.toSet(),
                undistributedAmount = if (undistributed != 0.0) Amount(undistributed, totalAmount.currencyCode) else null,
            )
        }
    }
}

private fun Collection<Share>.getMinShareCost(): Double {
    return filter { it.amount.value != 0.0 }.minOfOrNull { it.amount.value } ?: 0.0
}
