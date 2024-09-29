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
    fun initial_splitt_options_zeros() {
        val u1 = Participant(name = "a")
        val u2 = Participant(name = "b")
        val expense =
            createExpense(
                amount = 0f,
                shares =
                    setOf(
                        Share(u1, amount = Amount(0f, CURRENCY)),
                        Share(u2, amount = Amount(0f, CURRENCY)),
                    ),
            )

        val options = expense.getInitialSplitOptions()
        options.selectedSplitType shouldBe SplitType.EQUAL
        with(options.splitValues[SplitType.EQUAL]!!) {
            size shouldBe 2
            this[u1] shouldBe true
            this[u2] shouldBe true
        }
        with(options.splitValues[SplitType.SHARES]!!) {
            size shouldBe 2
            this[u1] shouldBe 1f
            this[u2] shouldBe 1f
        }
        with(options.splitValues[SplitType.AMOUNTS]!!) {
            size shouldBe 2
            this[u1] shouldBe 0f
            this[u2] shouldBe 0f
        }
    }

    @Test
    fun initial_splitt_options_non_zeros() {
        val u1 = Participant(name = "a")
        val u2 = Participant(name = "b")
        val expense =
            createExpense(
                amount = 30f,
                shares =
                    setOf(
                        Share(u1, amount = Amount(10f, CURRENCY)),
                        Share(u2, amount = Amount(20f, CURRENCY)),
                    ),
            )

        val options = expense.getInitialSplitOptions()
        options.selectedSplitType shouldBe SplitType.SHARES
        with(options.splitValues[SplitType.EQUAL]!!) {
            size shouldBe 2
            this[u1] shouldBe true
            this[u2] shouldBe true
        }
        with(options.splitValues[SplitType.SHARES]!!) {
            size shouldBe 2
            this[u1] shouldBe 1f
            this[u2] shouldBe 2f
        }
        with(options.splitValues[SplitType.AMOUNTS]!!) {
            size shouldBe 2
            this[u1] shouldBe 10f
            this[u2] shouldBe 20f
        }
    }

    @Test
    fun update_total_amount() {
        val u1 = Participant(name = "a")
        val u2 = Participant(name = "b")
        val expense =
            createExpense(
                amount = 30f,
                shares =
                    setOf(
                        Share(u1, amount = Amount(10f, CURRENCY)),
                        Share(u2, amount = Amount(20f, CURRENCY)),
                    ),
            )

        val options = expense.getInitialSplitOptions().update(UpdateAction.TotalAmount(150f))
        with(options.splitValues[SplitType.EQUAL]!!) {
            size shouldBe 2
            this[u1] shouldBe true
            this[u2] shouldBe true
        }
        with(options.splitValues[SplitType.SHARES]!!) {
            size shouldBe 2
            this[u1] shouldBe 1f
            this[u2] shouldBe 2f
        }
        with(options.splitValues[SplitType.AMOUNTS]!!) {
            size shouldBe 2
            this[u1] shouldBe 50f
            this[u2] shouldBe 100f
        }
    }

    @Test
    fun update_to_equal() {
        val u1 = Participant(name = "a")
        val u2 = Participant(name = "b")
        val u3 = Participant(name = "c")
        val expense =
            createExpense(
                amount = 90f,
                shares =
                    setOf(
                        Share(u1, amount = Amount(10f, CURRENCY)),
                        Share(u2, amount = Amount(30f, CURRENCY)),
                        Share(u3, amount = Amount(50f, CURRENCY)),
                    ),
            )

        val options = expense.getInitialSplitOptions().update(UpdateAction.Split.Equal(u1, false))
        with(options.splitValues[SplitType.EQUAL]!!) {
            size shouldBe 3
            this[u1] shouldBe false
            this[u2] shouldBe true
            this[u3] shouldBe true
        }
        with(options.splitValues[SplitType.SHARES]!!) {
            size shouldBe 3
            this[u1] shouldBe 0f
            this[u2] shouldBe 1f
            this[u3] shouldBe 1f
        }
        with(options.splitValues[SplitType.AMOUNTS]!!) {
            size shouldBe 3
            this[u1] shouldBe 0f
            this[u2] shouldBe 45f
            this[u3] shouldBe 45f
        }
    }

    @Test
    fun update_to_share() {
        val u1 = Participant(name = "a")
        val u2 = Participant(name = "b")
        val u3 = Participant(name = "c")
        val expense =
            createExpense(
                amount = 90f,
                shares =
                    setOf(
                        Share(u1, amount = Amount(30f, CURRENCY)),
                        Share(u2, amount = Amount(30f, CURRENCY)),
                        Share(u3, amount = Amount(30f, CURRENCY)),
                    ),
            )

        val options = expense.getInitialSplitOptions().update(UpdateAction.Split.Share(u1, 2f))
        with(options.splitValues[SplitType.EQUAL]!!) {
            size shouldBe 3
            this[u1] shouldBe true
            this[u2] shouldBe true
            this[u3] shouldBe true
        }
        with(options.splitValues[SplitType.SHARES]!!) {
            size shouldBe 3
            this[u1] shouldBe 2f
            this[u2] shouldBe 1f
            this[u3] shouldBe 1f
        }
        with(options.splitValues[SplitType.AMOUNTS]!!) {
            size shouldBe 3
            this[u1] shouldBe 45f
            this[u2] shouldBe 22.5f
            this[u3] shouldBe 22.5f
        }
    }

    @Test
    fun update_to_amount() {
        val u1 = Participant(name = "a")
        val u2 = Participant(name = "b")
        val u3 = Participant(name = "c")
        val expense =
            createExpense(
                amount = 90f,
                shares =
                    setOf(
                        Share(u1, amount = Amount(30f, CURRENCY)),
                        Share(u2, amount = Amount(30f, CURRENCY)),
                        Share(u3, amount = Amount(30f, CURRENCY)),
                    ),
            )

        val options = expense.getInitialSplitOptions().update(UpdateAction.Split.Amount(u1, 120f))
        with(options.splitValues[SplitType.EQUAL]!!) {
            size shouldBe 3
            this[u1] shouldBe true
            this[u2] shouldBe true
            this[u3] shouldBe true
        }
        with(options.splitValues[SplitType.SHARES]!!) {
            size shouldBe 3
            this[u1] shouldBe 4f
            this[u2] shouldBe 1f
            this[u3] shouldBe 1f
        }
        with(options.splitValues[SplitType.AMOUNTS]!!) {
            size shouldBe 3
            this[u1] shouldBe 120f
            this[u2] shouldBe 30f
            this[u3] shouldBe 30f
        }
    }
}

private fun createExpense(
    amount: Float,
    shares: Set<Share>,
    undistributed: Float = 0f,
) = Expense(
    title = "123",
    payedBy = Participant(name = "abc"),
    totalAmount = Amount(amount, CURRENCY),
    expenseType = ExpenseType.EXPENSE,
    undistributedAmount = Amount(undistributed, CURRENCY),
    shares = shares,
)
