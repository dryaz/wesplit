package app.wesplit.quicksplit

import app.wesplit.domain.model.group.Participant
import app.wesplit.quicksplit.QuickSplitCalculationUseCase.calculateParticipantShares
import app.wesplit.quicksplit.QuickSplitViewModel.State.Data.ShareItem
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class QuickSplitCalculationUseCaseTest {
    @Test
    fun testCalculateParticipantShares_basic() {
        // Participants
        val participant1 = Participant("Alice", "a")
        val participant2 = Participant("Bob", "b")

        // Share Items
        val shareItem = ShareItem(title = "Dinner", priceValue = 90.0)

        // Input Data
        val data =
            mapOf(
                shareItem to mapOf(participant1 to 2, participant2 to 1),
            )

        // Expected Results
        val expected =
            mapOf(
                participant1 to listOf(shareItem.copy(priceValue = 60.0)),
                participant2 to listOf(shareItem.copy(priceValue = 30.0)),
            )

        // Call the function
        val result = calculateParticipantShares(data)

        // Assert Results
        result shouldBe expected
    }

    @Test
    fun testCalculateParticipantShares_multipleShareItems() {
        // Participants
        val participant1 = Participant("Alice", "a")
        val participant2 = Participant("Bob", "b")
        val participant3 = Participant("Charlie", "c")

        // Share Items
        val shareItem1 = ShareItem(title = "Dinner", priceValue = 90.0)
        val shareItem2 = ShareItem(title = "Drinks", priceValue = 50.0)

        // Input Data
        val data =
            mapOf(
                shareItem1 to mapOf(participant1 to 2, participant2 to 1),
                shareItem2 to mapOf(participant2 to 1, participant3 to 1),
            )

        // Expected Results
        val expected =
            mapOf(
                participant1 to listOf(shareItem1.copy(priceValue = 60.0)),
                participant2 to
                    listOf(
                        shareItem1.copy(priceValue = 30.0),
                        shareItem2.copy(priceValue = 25.0),
                    ),
                participant3 to listOf(shareItem2.copy(priceValue = 25.0)),
            )

        // Call the function
        val result = calculateParticipantShares(data)

        // Assert Results
        result shouldBe expected
    }

    @Test
    fun testCalculateParticipantShares_noShares() {
        // Input Data
        val data = emptyMap<ShareItem, Map<Participant, Int>>()

        // Expected Results
        val expected = emptyMap<Participant, List<ShareItem>>()

        // Call the function
        val result = calculateParticipantShares(data)

        // Assert Results
        result shouldBe expected
    }

    @Test
    fun testCalculateParticipantShares_zeroTotalShares() {
        // Participants
        val participant1 = Participant("Alice", "a")

        // Share Items
        val shareItem = ShareItem(title = "Dinner", priceValue = 100.0)

        // Input Data
        val data =
            mapOf(
                shareItem to mapOf(participant1 to 0),
            )

        // Expected Results
        val expected = emptyMap<Participant, List<ShareItem>>()

        // Call the function
        val result = calculateParticipantShares(data)

        // Assert Results
        result shouldBe expected
    }

    @Test
    fun testCalculateParticipantShares_fractionalPriceValues() {
        // Participants
        val participant1 = Participant("Alice", "a")
        val participant2 = Participant("Bob", "b")

        // Share Items
        val shareItem = ShareItem(title = "Coffee", priceValue = 10.0)

        // Input Data
        val data =
            mapOf(
                shareItem to mapOf(participant1 to 1, participant2 to 3),
            )

        // Expected Results
        val expected =
            mapOf(
                participant1 to listOf(shareItem.copy(priceValue = 2.5)),
                participant2 to listOf(shareItem.copy(priceValue = 7.5)),
            )

        // Call the function
        val result = calculateParticipantShares(data)

        // Assert Results
        result shouldBe expected
    }
}
