package app.wesplit.expense

import app.wesplit.domain.model.currency.Amount
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
                amount = 0.0,
                shares =
                    setOf(
                        Share(u1, amount = Amount(0.0, CURRENCY)),
                        Share(u2, amount = Amount(0.0, CURRENCY)),
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
            this[u1] shouldBe 1.0
            this[u2] shouldBe 1.0
        }
        with(options.splitValues[SplitType.AMOUNTS]!!) {
            size shouldBe 2
            this[u1] shouldBe 0.0
            this[u2] shouldBe 0.0
        }
    }

    @Test
    fun initial_splitt_options_non_zeros() {
        val u1 = Participant(name = "a")
        val u2 = Participant(name = "b")
        val expense =
            createExpense(
                amount = 30.0,
                shares =
                    setOf(
                        Share(u1, amount = Amount(10.0, CURRENCY)),
                        Share(u2, amount = Amount(20.0, CURRENCY)),
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
            this[u1] shouldBe 1.0
            this[u2] shouldBe 2.0
        }
        with(options.splitValues[SplitType.AMOUNTS]!!) {
            size shouldBe 2
            this[u1] shouldBe 10.0
            this[u2] shouldBe 20.0
        }
    }

    @Test
    fun update_total_amount() {
        val u1 = Participant(name = "a")
        val u2 = Participant(name = "b")
        val expense =
            createExpense(
                amount = 30.0,
                shares =
                    setOf(
                        Share(u1, amount = Amount(10.0, CURRENCY)),
                        Share(u2, amount = Amount(20.0, CURRENCY)),
                    ),
            )

        val options = expense.getInitialSplitOptions().update(UpdateAction.TotalAmount(150.0))
        with(options.splitValues[SplitType.EQUAL]!!) {
            size shouldBe 2
            this[u1] shouldBe true
            this[u2] shouldBe true
        }
        with(options.splitValues[SplitType.SHARES]!!) {
            size shouldBe 2
            this[u1] shouldBe 1.0
            this[u2] shouldBe 2.0
        }
        with(options.splitValues[SplitType.AMOUNTS]!!) {
            size shouldBe 2
            this[u1] shouldBe 50.0
            this[u2] shouldBe 100.0
        }
    }

    @Test
    fun update_to_equal() {
        val u1 = Participant(name = "a")
        val u2 = Participant(name = "b")
        val u3 = Participant(name = "c")
        val expense =
            createExpense(
                amount = 90.0,
                shares =
                    setOf(
                        Share(u1, amount = Amount(10.0, CURRENCY)),
                        Share(u2, amount = Amount(30.0, CURRENCY)),
                        Share(u3, amount = Amount(50.0, CURRENCY)),
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
            this[u1] shouldBe 0.0
            this[u2] shouldBe 1.0
            this[u3] shouldBe 1.0
        }
        with(options.splitValues[SplitType.AMOUNTS]!!) {
            size shouldBe 3
            this[u1] shouldBe 0.0
            this[u2] shouldBe 45.0
            this[u3] shouldBe 45.0
        }
    }

    @Test
    fun update_to_share() {
        val u1 = Participant(name = "a")
        val u2 = Participant(name = "b")
        val u3 = Participant(name = "c")
        val expense =
            createExpense(
                amount = 90.0,
                shares =
                    setOf(
                        Share(u1, amount = Amount(30.0, CURRENCY)),
                        Share(u2, amount = Amount(30.0, CURRENCY)),
                        Share(u3, amount = Amount(30.0, CURRENCY)),
                    ),
            )

        val options = expense.getInitialSplitOptions().update(UpdateAction.Split.Share(u1, 2.0))
        with(options.splitValues[SplitType.EQUAL]!!) {
            size shouldBe 3
            this[u1] shouldBe true
            this[u2] shouldBe true
            this[u3] shouldBe true
        }
        with(options.splitValues[SplitType.SHARES]!!) {
            size shouldBe 3
            this[u1] shouldBe 2.0
            this[u2] shouldBe 1.0
            this[u3] shouldBe 1.0
        }
        with(options.splitValues[SplitType.AMOUNTS]!!) {
            size shouldBe 3
            this[u1] shouldBe 45.0
            this[u2] shouldBe 22.5
            this[u3] shouldBe 22.5
        }
    }

    @Test
    fun update_to_amount() {
        val u1 = Participant(name = "a")
        val u2 = Participant(name = "b")
        val u3 = Participant(name = "c")
        val expense =
            createExpense(
                amount = 90.0,
                shares =
                    setOf(
                        Share(u1, amount = Amount(30.0, CURRENCY)),
                        Share(u2, amount = Amount(30.0, CURRENCY)),
                        Share(u3, amount = Amount(30.0, CURRENCY)),
                    ),
            )

        val options = expense.getInitialSplitOptions().update(UpdateAction.Split.Amount(u1, 120.0))
        with(options.splitValues[SplitType.EQUAL]!!) {
            size shouldBe 3
            this[u1] shouldBe true
            this[u2] shouldBe true
            this[u3] shouldBe true
        }
        with(options.splitValues[SplitType.SHARES]!!) {
            size shouldBe 3
            this[u1] shouldBe 4.0
            this[u2] shouldBe 1.0
            this[u3] shouldBe 1.0
        }
        with(options.splitValues[SplitType.AMOUNTS]!!) {
            size shouldBe 3
            this[u1] shouldBe 120.0
            this[u2] shouldBe 30.0
            this[u3] shouldBe 30.0
        }
    }

    @Test
    fun initial_equal_empty_shares_non_zero_value() {
        val u1 = Participant(name = "a")
        val u2 = Participant(name = "b")
        val expense =
            createExpense(
                amount = 100.0,
                shares =
                    setOf(
                        Share(u1, amount = Amount(0.0, CURRENCY)),
                        Share(u2, amount = Amount(0.0, CURRENCY)),
                    ),
            )

        val options = expense.getInitialSplitOptions()
        with(options.splitValues[SplitType.EQUAL]!!) {
            size shouldBe 2
            this[u1] shouldBe false
            this[u2] shouldBe false
        }
        with(options.splitValues[SplitType.SHARES]!!) {
            size shouldBe 2
            this[u1] shouldBe 1.0
            this[u2] shouldBe 1.0
        }
        with(options.splitValues[SplitType.AMOUNTS]!!) {
            size shouldBe 2
            this[u1] shouldBe 0.0
            this[u2] shouldBe 0.0
        }
    }

    @Test
    fun update_equal_from_zeros() {
        val u1 = Participant(name = "a")
        val u2 = Participant(name = "b")
        val expense =
            createExpense(
                amount = 10.0,
                shares =
                    setOf(
                        Share(u1, amount = Amount(0.0, CURRENCY)),
                        Share(u2, amount = Amount(0.0, CURRENCY)),
                    ),
            )

        val options = expense.getInitialSplitOptions().update(UpdateAction.Split.Equal(u1, true))
        with(options.splitValues[SplitType.EQUAL]!!) {
            size shouldBe 2
            this[u1] shouldBe true
            this[u2] shouldBe false
        }
        with(options.splitValues[SplitType.SHARES]!!) {
            size shouldBe 2
            this[u1] shouldBe 1.0
            this[u2] shouldBe 0.0
        }
        with(options.splitValues[SplitType.AMOUNTS]!!) {
            size shouldBe 2
            this[u1] shouldBe 0.0
            this[u2] shouldBe 0.0
        }
    }

    @Test
    fun initial_splitt_options_non_zeros_extra_participants() {
        val u1 = Participant(name = "a")
        val u2 = Participant(name = "b")
        val u3 = Participant(name = "c")
        val expense =
            createExpense(
                amount = 30.0,
                shares =
                    setOf(
                        Share(u1, amount = Amount(10.0, CURRENCY)),
                        Share(u2, amount = Amount(20.0, CURRENCY)),
                    ),
            )

        val options = expense.getInitialSplitOptions(listOf(u3))
        options.selectedSplitType shouldBe SplitType.SHARES
        with(options.splitValues[SplitType.EQUAL]!!) {
            size shouldBe 3
            this[u1] shouldBe true
            this[u2] shouldBe true
            this[u3] shouldBe false
        }
        with(options.splitValues[SplitType.SHARES]!!) {
            size shouldBe 3
            this[u1] shouldBe 1.0
            this[u2] shouldBe 2.0
            this[u3] shouldBe 0.0
        }
        with(options.splitValues[SplitType.AMOUNTS]!!) {
            size shouldBe 3
            this[u1] shouldBe 10.0
            this[u2] shouldBe 20.0
            this[u3] shouldBe 0.0
        }
    }

    @Test
    fun recalculate_shares_for_equal_split_options() {
        val u1 = Participant(name = "a")
        val u2 = Participant(name = "b")
        val expense =
            createExpense(
                amount = 30.0,
                shares =
                    setOf(
                        Share(u1, amount = Amount(0.0, CURRENCY)),
                        Share(u2, amount = Amount(0.0, CURRENCY)),
                    ),
            )

        val newExpense =
            expense.reCalculateShares(
                ExpenseDetailsViewModel.State.Data.SplitOptions(
                    selectedSplitType = SplitType.EQUAL,
                    splitValues =
                        mapOf(
                            SplitType.EQUAL to
                                mapOf(
                                    u1 to true,
                                    u2 to false,
                                ),
                            SplitType.SHARES to
                                mapOf(
                                    u1 to 0.0,
                                    u2 to 0.0,
                                ),
                            SplitType.AMOUNTS to
                                mapOf(
                                    u1 to 0.0,
                                    u2 to 0.0,
                                ),
                        ),
                ),
            )

        newExpense.shares.first { it.participant == u1 }.amount.value shouldBe 30.0
        newExpense.shares.first { it.participant == u2 }.amount.value shouldBe 0.0
    }

    @Test
    fun recalculate_shares_for_shares_split_options() {
        val u1 = Participant(name = "a")
        val u2 = Participant(name = "b")
        val expense =
            createExpense(
                amount = 30.0,
                shares =
                    setOf(
                        Share(u1, amount = Amount(0.0, CURRENCY)),
                        Share(u2, amount = Amount(0.0, CURRENCY)),
                    ),
            )

        val newExpense =
            expense.reCalculateShares(
                ExpenseDetailsViewModel.State.Data.SplitOptions(
                    selectedSplitType = SplitType.SHARES,
                    splitValues =
                        mapOf(
                            SplitType.EQUAL to
                                mapOf(
                                    u1 to false,
                                    u2 to false,
                                ),
                            SplitType.SHARES to
                                mapOf(
                                    u1 to 1.0,
                                    u2 to 0.0,
                                ),
                            SplitType.AMOUNTS to
                                mapOf(
                                    u1 to 0.0,
                                    u2 to 0.0,
                                ),
                        ),
                ),
            )

        newExpense.shares.first { it.participant == u1 }.amount.value shouldBe 30.0
        newExpense.shares.first { it.participant == u2 }.amount.value shouldBe 0.0
    }

    @Test
    fun recalculate_shares_for_shares_split_options_zeros() {
        val u1 = Participant(name = "a")
        val u2 = Participant(name = "b")
        val expense =
            createExpense(
                amount = 30.0,
                shares =
                    setOf(
                        Share(u1, amount = Amount(0.0, CURRENCY)),
                        Share(u2, amount = Amount(0.0, CURRENCY)),
                    ),
            )

        val newExpense =
            expense.reCalculateShares(
                ExpenseDetailsViewModel.State.Data.SplitOptions(
                    selectedSplitType = SplitType.SHARES,
                    splitValues =
                        mapOf(
                            SplitType.EQUAL to
                                mapOf(
                                    u1 to false,
                                    u2 to false,
                                ),
                            SplitType.SHARES to
                                mapOf(
                                    u1 to 0.0,
                                    u2 to 0.0,
                                ),
                            SplitType.AMOUNTS to
                                mapOf(
                                    u1 to 0.0,
                                    u2 to 0.0,
                                ),
                        ),
                ),
            )

        newExpense.shares.first { it.participant == u1 }.amount.value shouldBe 0.0
        newExpense.shares.first { it.participant == u2 }.amount.value shouldBe 0.0

        newExpense.undistributedAmount?.value shouldBe 30.0
    }

    @Test
    fun recalculate_shares_for_equal_split_options_zeros() {
        val u1 = Participant(name = "a")
        val u2 = Participant(name = "b")
        val expense =
            createExpense(
                amount = 30.0,
                shares =
                    setOf(
                        Share(u1, amount = Amount(0.0, CURRENCY)),
                        Share(u2, amount = Amount(0.0, CURRENCY)),
                    ),
            )

        val newExpense =
            expense.reCalculateShares(
                ExpenseDetailsViewModel.State.Data.SplitOptions(
                    selectedSplitType = SplitType.EQUAL,
                    splitValues =
                        mapOf(
                            SplitType.EQUAL to
                                mapOf(
                                    u1 to false,
                                    u2 to false,
                                ),
                            SplitType.SHARES to
                                mapOf(
                                    u1 to 0.0,
                                    u2 to 0.0,
                                ),
                            SplitType.AMOUNTS to
                                mapOf(
                                    u1 to 0.0,
                                    u2 to 0.0,
                                ),
                        ),
                ),
            )

        newExpense.shares.first { it.participant == u1 }.amount.value shouldBe 0.0
        newExpense.shares.first { it.participant == u2 }.amount.value shouldBe 0.0

        newExpense.undistributedAmount?.value shouldBe 30.0
    }

    @Test
    fun recalculate_shares_for_amount_split_options() {
        val u1 = Participant(name = "a")
        val u2 = Participant(name = "b")
        val expense =
            createExpense(
                amount = 30.0,
                shares =
                    setOf(
                        Share(u1, amount = Amount(0.0, CURRENCY)),
                        Share(u2, amount = Amount(0.0, CURRENCY)),
                    ),
            )

        val newExpense =
            expense.reCalculateShares(
                ExpenseDetailsViewModel.State.Data.SplitOptions(
                    selectedSplitType = SplitType.AMOUNTS,
                    splitValues =
                        mapOf(
                            SplitType.EQUAL to
                                mapOf(
                                    u1 to false,
                                    u2 to false,
                                ),
                            SplitType.SHARES to
                                mapOf(
                                    u1 to 0.0,
                                    u2 to 0.0,
                                ),
                            SplitType.AMOUNTS to
                                mapOf(
                                    u1 to 10.0,
                                    u2 to 10.0,
                                ),
                        ),
                ),
            )

        newExpense.shares.first { it.participant == u1 }.amount.value shouldBe 10.0
        newExpense.shares.first { it.participant == u2 }.amount.value shouldBe 10.0

        newExpense.undistributedAmount?.value shouldBe 10.0
    }
}

private fun createExpense(
    amount: Double,
    shares: Set<Share>,
    undistributed: Double = 0.0,
) = Expense(
    title = "123",
    payedBy = Participant(name = "abc"),
    totalAmount = Amount(amount, CURRENCY),
    expenseType = ExpenseType.EXPENSE,
    undistributedAmount = Amount(undistributed, CURRENCY),
    shares = shares,
)
