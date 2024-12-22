package app.wesplit.quicksplit

import app.wesplit.domain.model.group.Participant

object QuickSplitCalculationUseCase {
    fun calculateParticipantShares(
        data: Map<QuickSplitViewModel.State.Data.ShareItem, Map<Participant, Int>>,
    ): Map<Participant, List<QuickSplitViewModel.State.Data.ShareItem>> {
        val result = mutableMapOf<Participant, MutableList<QuickSplitViewModel.State.Data.ShareItem>>()

        for ((shareItem, participantShares) in data) {
            val totalShares = participantShares.values.sum()

            for ((participant, shareCount) in participantShares) {
                if (totalShares > 0) {
                    val recalculatedPrice = shareItem.priceValue * (shareCount.toDouble() / totalShares)
                    val recalculatedShareItem = shareItem.copy(priceValue = recalculatedPrice)

                    // Manually handle computeIfAbsent-like behavior
                    val participantList = result[participant]
                    if (participantList == null) {
                        result[participant] = mutableListOf(recalculatedShareItem)
                    } else {
                        participantList.add(recalculatedShareItem)
                    }
                }
            }
        }

        return result
    }
}
