package app.wesplit.expense

import app.wesplit.domain.model.expense.Amount
import app.wesplit.domain.model.expense.Expense
import app.wesplit.domain.model.expense.ExpenseType
import app.wesplit.domain.model.expense.Share
import app.wesplit.domain.model.expense.SplitType
import app.wesplit.domain.model.group.Participant
import io.kotest.matchers.shouldBe
import kotlin.test.Test

private const val CURRENCY = "U"

class ExpenseUtilsTest {
    @Test
    fun redistribute_equal_split() {
        val expense =
            createExpense(
                300f,
                setOf(
                    Share(Participant(name = "a"), Amount(100f, CURRENCY)),
                    Share(Participant(name = "a"), Amount(100f, CURRENCY)),
                    Share(Participant(name = "a"), Amount(100f, CURRENCY)),
                ),
                splitType = SplitType.EQUAL,
            )

        val newExpense = expense.reCalculateShares()
        newExpense.shares.size shouldBe 3
        newExpense.shares.forEach {
            it.amount.value shouldBe 100f
        }
        (newExpense.undistributedAmount?.value ?: 0f) shouldBe 0f
        newExpense.splitType shouldBe SplitType.EQUAL
    }

    @Test
    fun redistribute_equal_split_new_amount() {
        val expense =
            createExpense(
                300f,
                setOf(
                    Share(Participant(name = "a"), Amount(100f, CURRENCY)),
                    Share(Participant(name = "a"), Amount(100f, CURRENCY)),
                    Share(Participant(name = "a"), Amount(100f, CURRENCY)),
                ),
                splitType = SplitType.EQUAL,
            )

        val newExpense = expense.copy(totalAmount = Amount(450f, CURRENCY)).reCalculateShares()
        newExpense.shares.size shouldBe 3
        newExpense.shares.forEach {
            it.amount.value shouldBe 150f
        }
        (newExpense.undistributedAmount?.value ?: 0f) shouldBe 0f
        newExpense.splitType shouldBe SplitType.EQUAL
    }

    @Test
    fun redistribute_shares_split() {
        val expense =
            createExpense(
                600f,
                setOf(
                    Share(Participant(name = "a"), Amount(100f, CURRENCY)),
                    Share(Participant(name = "b"), Amount(200f, CURRENCY)),
                    Share(Participant(name = "c"), Amount(300f, CURRENCY)),
                ),
                splitType = SplitType.SHARES,
            )

        val newExpense = expense.reCalculateShares()
        newExpense.shares.size shouldBe 3
        newExpense.shares.first { it.participant.name == "a" }.amount.value shouldBe 100f
        newExpense.shares.first { it.participant.name == "b" }.amount.value shouldBe 200f
        newExpense.shares.first { it.participant.name == "c" }.amount.value shouldBe 300f
        (newExpense.undistributedAmount?.value ?: 0f) shouldBe 0
        newExpense.splitType shouldBe SplitType.SHARES
    }

    @Test
    fun redistribute_shares_split_change_total() {
        val expense =
            createExpense(
                600f,
                setOf(
                    Share(Participant(name = "a"), Amount(100f, CURRENCY)),
                    Share(Participant(name = "b"), Amount(200f, CURRENCY)),
                    Share(Participant(name = "c"), Amount(300f, CURRENCY)),
                ),
                splitType = SplitType.SHARES,
            )

        val newExpense = expense.copy(totalAmount = Amount(900f, CURRENCY)).reCalculateShares()
        newExpense.shares.size shouldBe 3
        newExpense.shares.first { it.participant.name == "a" }.amount.value shouldBe 150f
        newExpense.shares.first { it.participant.name == "b" }.amount.value shouldBe 300f
        newExpense.shares.first { it.participant.name == "c" }.amount.value shouldBe 450f
        (newExpense.undistributedAmount?.value ?: 0f) shouldBe 0f
        newExpense.splitType shouldBe SplitType.SHARES
    }

    @Test
    fun redistribute_shares_split_change_share() {
        val expense =
            createExpense(
                600f,
                setOf(
                    Share(Participant(name = "a"), Amount(100f, CURRENCY)),
                    Share(Participant(name = "b"), Amount(200f, CURRENCY)),
                    Share(Participant(name = "c"), Amount(300f, CURRENCY)),
                ),
                splitType = SplitType.SHARES,
            )

        val participantToChange = expense.shares.first { it.participant.name == "a" }.participant
        val newExpense = expense.reCalculateShares(UpdateAction.Split.Share(participantToChange, 3f))
        newExpense.shares.size shouldBe 3
        newExpense.shares.first { it.participant.name == "a" }.amount.value shouldBe 225f
        newExpense.shares.first { it.participant.name == "b" }.amount.value shouldBe 150f
        newExpense.shares.first { it.participant.name == "c" }.amount.value shouldBe 225f
        (newExpense.undistributedAmount?.value ?: 0f) shouldBe 0f
        newExpense.splitType shouldBe SplitType.SHARES
    }

    @Test
    fun redistribute_shares_split_change_share_type_equal_include() {
        val expense =
            createExpense(
                600f,
                setOf(
                    Share(Participant(name = "a"), Amount(100f, CURRENCY)),
                    Share(Participant(name = "b"), Amount(200f, CURRENCY)),
                    Share(Participant(name = "c"), Amount(300f, CURRENCY)),
                ),
                splitType = SplitType.SHARES,
            )

        val participantToChange = expense.shares.first { it.participant.name == "a" }.participant
        val newExpense = expense.reCalculateShares(UpdateAction.Split.Equal(participantToChange, true))
        newExpense.shares.size shouldBe 3
        newExpense.shares.first { it.participant.name == "a" }.amount.value shouldBe 200f
        newExpense.shares.first { it.participant.name == "b" }.amount.value shouldBe 200f
        newExpense.shares.first { it.participant.name == "c" }.amount.value shouldBe 200f
        (newExpense.undistributedAmount?.value ?: 0f) shouldBe 0f
        newExpense.splitType shouldBe SplitType.EQUAL
    }

    @Test
    fun redistribute_shares_split_change_share_type_eqaul_exclude() {
        val expense =
            createExpense(
                600f,
                setOf(
                    Share(Participant(name = "a"), Amount(100f, CURRENCY)),
                    Share(Participant(name = "b"), Amount(200f, CURRENCY)),
                    Share(Participant(name = "c"), Amount(300f, CURRENCY)),
                ),
                splitType = SplitType.SHARES,
            )

        val participantToChange = expense.shares.first { it.participant.name == "a" }.participant
        val newExpense = expense.reCalculateShares(UpdateAction.Split.Equal(participantToChange, false))
        newExpense.shares.size shouldBe 2
        newExpense.shares.first { it.participant.name == "b" }.amount.value shouldBe 300f
        newExpense.shares.first { it.participant.name == "c" }.amount.value shouldBe 300f
        (newExpense.undistributedAmount?.value ?: 0f) shouldBe 0f
        newExpense.splitType shouldBe SplitType.EQUAL
    }

    @Test
    fun redistribute_equal_split_change_to_shares() {
        val expense =
            createExpense(
                300f,
                setOf(
                    Share(Participant(name = "a"), Amount(100f, CURRENCY)),
                    Share(Participant(name = "b"), Amount(100f, CURRENCY)),
                    Share(Participant(name = "c"), Amount(100f, CURRENCY)),
                ),
                splitType = SplitType.EQUAL,
            )

        val participantToChange = expense.shares.first { it.participant.name == "a" }.participant
        val newExpense = expense.reCalculateShares(UpdateAction.Split.Share(participantToChange, 2f))
        newExpense.shares.size shouldBe 3
        newExpense.shares.first { it.participant.name == "a" }.amount.value shouldBe 150f
        newExpense.shares.first { it.participant.name == "b" }.amount.value shouldBe 75f
        newExpense.shares.first { it.participant.name == "c" }.amount.value shouldBe 75f
        (newExpense.undistributedAmount?.value ?: 0f) shouldBe 0f
        newExpense.splitType shouldBe SplitType.SHARES
    }

    @Test
    fun redistribute_shares_split_change_share_to_zero() {
        val expense =
            createExpense(
                600f,
                setOf(
                    Share(Participant(name = "a"), Amount(100f, CURRENCY)),
                    Share(Participant(name = "b"), Amount(200f, CURRENCY)),
                    Share(Participant(name = "c"), Amount(300f, CURRENCY)),
                ),
                splitType = SplitType.SHARES,
            )

        val participantToChange = expense.shares.first { it.participant.name == "a" }.participant
        val newExpense = expense.reCalculateShares(UpdateAction.Split.Share(participantToChange, 0f))
        newExpense.shares.size shouldBe 3
        newExpense.shares.first { it.participant.name == "a" }.amount.value shouldBe 0f
        newExpense.shares.first { it.participant.name == "b" }.amount.value shouldBe 240f
        newExpense.shares.first { it.participant.name == "c" }.amount.value shouldBe 360f
        (newExpense.undistributedAmount?.value ?: 0f) shouldBe 0f
        newExpense.splitType shouldBe SplitType.SHARES
    }
}

private fun createExpense(
    amount: Float,
    shares: Set<Share>,
    splitType: SplitType,
    undistributed: Float = 0f,
) = Expense(
    title = "123",
    payedBy = Participant(name = "abc"),
    totalAmount = Amount(amount, CURRENCY),
    expenseType = ExpenseType.EXPENSE,
    undistributedAmount = Amount(undistributed, CURRENCY),
    shares = shares,
    splitType = splitType,
)
