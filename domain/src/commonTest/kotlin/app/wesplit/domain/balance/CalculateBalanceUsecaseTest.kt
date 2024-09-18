package app.wesplit.domain.balance

import app.wesplit.domain.model.expense.Amount
import app.wesplit.domain.model.expense.Expense
import app.wesplit.domain.model.expense.ExpenseType
import app.wesplit.domain.model.expense.Share
import app.wesplit.domain.model.group.Group
import app.wesplit.domain.model.group.Participant
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class CalculateBalanceUsecaseTest {
    @Test
    fun not_shared_exp_undistributed() {
        val p1 = Participant("1", "U1")
        val group =
            Group(
                id = "123",
                title = "234",
                participants =
                    setOf(
                        p1,
                        Participant("2", "U2"),
                    ),
            )
        val expenseList =
            listOf(
                Expense(
                    "1",
                    "e1",
                    payedBy = p1,
                    shares = emptySet(),
                    totalAmount = Amount(100f, "USD"),
                    expenseType = ExpenseType.EXPENSE,
                    undistributedAmount = Amount(100f, "USD"),
                ),
            )

        val balance = CalculateBalanceUsecase().invoke(group, expenseList)
        balance.nonDistributed.value shouldBe 100f
    }

    @Test
    fun not_shared_exp_recorded_to_payer() {
        val p1 = Participant("1", "U1")
        val group =
            Group(
                id = "123",
                title = "234",
                participants =
                    setOf(
                        p1,
                        Participant("2", "U2"),
                    ),
            )
        val expenseList =
            listOf(
                Expense(
                    "1",
                    "e1",
                    payedBy = p1,
                    shares = emptySet(),
                    totalAmount = Amount(100f, "USD"),
                    expenseType = ExpenseType.EXPENSE,
                    undistributedAmount = Amount(100f, "USD"),
                ),
            )

        val balance = CalculateBalanceUsecase().invoke(group, expenseList)
        balance.participants[p1]?.balance?.value shouldBe 100f
    }

    @Test
    fun equal_payment_should_lead_to_0_balance() {
        val p1 = Participant("1", "U1")
        val p2 = Participant("2", "U2")
        val group =
            Group(
                id = "123",
                title = "234",
                participants =
                    setOf(
                        p1,
                        p2,
                    ),
            )
        val expenseList =
            listOf(
                Expense(
                    "1",
                    "e1",
                    payedBy = p1,
                    shares =
                        setOf(
                            Share(p1, Amount(50f, "USD")),
                            Share(p2, Amount(50f, "USD")),
                        ),
                    totalAmount = Amount(100f, "USD"),
                    expenseType = ExpenseType.EXPENSE,
                    undistributedAmount = null,
                ),
                Expense(
                    "2",
                    "e2",
                    payedBy = p2,
                    shares =
                        setOf(
                            Share(p1, Amount(50f, "USD")),
                            Share(p2, Amount(50f, "USD")),
                        ),
                    totalAmount = Amount(100f, "USD"),
                    expenseType = ExpenseType.EXPENSE,
                    undistributedAmount = null,
                ),
            )

        val balance = CalculateBalanceUsecase().invoke(group, expenseList)
        balance.participants[p1]?.balance?.value shouldBe 0f
        balance.participants[p2]?.balance?.value shouldBe 0f
        balance.nonDistributed.value shouldBe 0f
    }
}
