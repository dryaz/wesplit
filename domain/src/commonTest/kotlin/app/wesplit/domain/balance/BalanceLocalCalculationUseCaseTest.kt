package app.wesplit.domain.balance

import app.wesplit.domain.model.currency.Amount
import app.wesplit.domain.model.expense.Expense
import app.wesplit.domain.model.expense.ExpenseStatus
import app.wesplit.domain.model.expense.ExpenseType
import app.wesplit.domain.model.expense.Share
import app.wesplit.domain.model.group.Participant
import io.kotest.matchers.shouldBe
import kotlin.random.Random
import kotlin.test.Test

class BalanceLocalCalculationUseCaseTest {
    @Test
    fun `empty_expense_list_mapped_to_empty_balances`() {
        val useCase = BalanceLocalCalculationUseCase()
        val result = useCase.invoke(emptyList())
        result.participantsBalance shouldBe emptySet()
        result.undistributed shouldBe emptySet()
        result.invalid shouldBe false
    }

    @Test
    fun `single_expense_for_payer_only_empty_balance`() {
        val useCase = BalanceLocalCalculationUseCase()
        val participant = Participant("a", "b")
        val amount = Amount(100.0, "USD")
        val expense =
            createExpense(
                payedBy = participant,
                shares =
                    setOf(
                        Share(
                            participant = participant,
                            amount = amount,
                        ),
                    ),
                totalAmount = amount,
                status = ExpenseStatus.NEW,
            )
        val result = useCase.invoke(listOf(expense))
        result.participantsBalance shouldBe emptySet()
        result.undistributed shouldBe emptySet()
        result.invalid shouldBe false
    }

    @Test
    fun `single_expense_for_payer_only_undist_non_empty_undistr`() {
        val useCase = BalanceLocalCalculationUseCase()
        val participant = Participant("a", "b")
        val expense =
            createExpense(
                payedBy = participant,
                shares =
                    setOf(
                        Share(
                            participant = participant,
                            amount = Amount(50.0, "USD"),
                        ),
                    ),
                totalAmount = Amount(100.0, "USD"),
                undistributedAmount = Amount(50.0, "USD"),
                status = ExpenseStatus.NEW,
            )
        val result = useCase.invoke(listOf(expense))
        result.participantsBalance.size shouldBe 1
        with(result.participantsBalance.first()) {
            this.participant shouldBe participant
            this.amounts.size shouldBe 1
            with(this.amounts.first()) {
                this.value shouldBe 50.0
                this.currencyCode shouldBe "USD"
            }
        }
        result.undistributed.size shouldBe 1
        with(result.undistributed.first()) {
            this.value shouldBe 50.0
            this.currencyCode shouldBe "USD"
        }
        result.invalid shouldBe false
    }

    @Test
    fun `even_2_trx_split`() {
        val useCase = BalanceLocalCalculationUseCase()
        val participant1 = Participant("a", "b")
        val participant2 = Participant("b", "c")
        val result =
            useCase.invoke(
                listOf(
                    createExpense(
                        payedBy = participant1,
                        shares =
                            setOf(
                                Share(
                                    participant = participant1,
                                    amount = Amount(50.0, "USD"),
                                ),
                                Share(
                                    participant = participant2,
                                    amount = Amount(50.0, "USD"),
                                ),
                            ),
                        totalAmount = Amount(100.0, "USD"),
                        status = ExpenseStatus.NEW,
                    ),
                    createExpense(
                        payedBy = participant2,
                        shares =
                            setOf(
                                Share(
                                    participant = participant1,
                                    amount = Amount(50.0, "USD"),
                                ),
                                Share(
                                    participant = participant2,
                                    amount = Amount(50.0, "USD"),
                                ),
                            ),
                        totalAmount = Amount(100.0, "USD"),
                        status = ExpenseStatus.NEW,
                    ),
                ),
            )
        result.participantsBalance.size shouldBe 2
        result.participantsBalance.forEach {
            it.amounts shouldBe emptySet()
        }
        result.undistributed shouldBe emptySet()
        result.invalid shouldBe false
    }

    @Test
    fun `positive_negative_balance_values`() {
        val useCase = BalanceLocalCalculationUseCase()
        val participant1 = Participant("a", "b")
        val participant2 = Participant("b", "c")
        val result =
            useCase.invoke(
                listOf(
                    createExpense(
                        payedBy = participant1,
                        shares =
                            setOf(
                                Share(
                                    participant = participant1,
                                    amount = Amount(50.0, "USD"),
                                ),
                                Share(
                                    participant = participant2,
                                    amount = Amount(50.0, "USD"),
                                ),
                            ),
                        totalAmount = Amount(100.0, "USD"),
                        status = ExpenseStatus.NEW,
                    ),
                ),
            )
        result.participantsBalance.size shouldBe 2
        with(result.participantsBalance.first { it.participant == participant1 }) {
            this.amounts.size shouldBe 1
            with(this.amounts.first()) {
                this.currencyCode shouldBe "USD"
                this.value shouldBe 50.0
            }
        }
        with(result.participantsBalance.first { it.participant == participant2 }) {
            this.amounts.size shouldBe 1
            with(this.amounts.first()) {
                this.currencyCode shouldBe "USD"
                this.value shouldBe -50.0
            }
        }
        result.undistributed shouldBe emptySet()
        result.invalid shouldBe false
    }

    @Test
    fun `differet_cur_2_trx_split`() {
        val useCase = BalanceLocalCalculationUseCase()
        val participant1 = Participant("a", "b")
        val participant2 = Participant("b", "c")
        val result =
            useCase.invoke(
                listOf(
                    createExpense(
                        payedBy = participant1,
                        shares =
                            setOf(
                                Share(
                                    participant = participant1,
                                    amount = Amount(50.0, "USD"),
                                ),
                                Share(
                                    participant = participant2,
                                    amount = Amount(50.0, "USD"),
                                ),
                            ),
                        totalAmount = Amount(100.0, "USD"),
                        status = ExpenseStatus.NEW,
                    ),
                    createExpense(
                        payedBy = participant2,
                        shares =
                            setOf(
                                Share(
                                    participant = participant1,
                                    amount = Amount(50.0, "EUR"),
                                ),
                                Share(
                                    participant = participant2,
                                    amount = Amount(50.0, "EUR"),
                                ),
                            ),
                        totalAmount = Amount(100.0, "EUR"),
                        status = ExpenseStatus.NEW,
                    ),
                ),
            )
        result.participantsBalance.size shouldBe 2
        with(result.participantsBalance.first { it.participant == participant1 }) {
            this.amounts.size shouldBe 2
            with(this.amounts.first { it.currencyCode == "USD" }) {
                this.currencyCode shouldBe "USD"
                this.value shouldBe 50.0
            }
            with(this.amounts.first { it.currencyCode == "EUR" }) {
                this.currencyCode shouldBe "EUR"
                this.value shouldBe -50.0
            }
        }
        with(result.participantsBalance.first { it.participant == participant2 }) {
            this.amounts.size shouldBe 2
            with(this.amounts.first { it.currencyCode == "USD" }) {
                this.currencyCode shouldBe "USD"
                this.value shouldBe -50.0
            }
            with(this.amounts.first { it.currencyCode == "EUR" }) {
                this.currencyCode shouldBe "EUR"
                this.value shouldBe 50.0
            }
        }
        result.undistributed shouldBe emptySet()
        result.invalid shouldBe false
    }
}

private fun createExpense(
    payedBy: Participant,
    shares: Set<Share>,
    status: ExpenseStatus,
    undistributedAmount: Amount? = null,
    totalAmount: Amount,
) = Expense(
    title = Random.nextLong().toString(),
    expenseType = ExpenseType.EXPENSE,
    payedBy = payedBy,
    undistributedAmount = undistributedAmount,
    status = status,
    shares = shares,
    totalAmount = totalAmount,
)
