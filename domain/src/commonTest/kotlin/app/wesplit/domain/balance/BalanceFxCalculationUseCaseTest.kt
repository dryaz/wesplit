package app.wesplit.domain.balance

import app.wesplit.domain.model.currency.Amount
import app.wesplit.domain.model.currency.FxRates
import app.wesplit.domain.model.group.Balance
import app.wesplit.domain.model.group.Participant
import app.wesplit.domain.model.group.ParticipantBalance
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class BalanceFxCalculationUseCaseTest {
    @Test
    fun `map_currency_to_base_value`() {
        val useCase = BalanceFxCalculationUseCase()
        val balance =
            Balance(
                participantsBalance =
                    setOf(
                        ParticipantBalance(
                            participant = Participant("1", "a"),
                            amounts =
                                setOf(
                                    Amount(20.0, "USD"),
                                    Amount(30.0, "EUR"),
                                    Amount(40.0, "GBP"),
                                    Amount(50.0, "RUB"),
                                ),
                        ),
                    ),
                undistributed =
                    setOf(
                        Amount(20.0, "USD"),
                        Amount(30.0, "EUR"),
                        Amount(40.0, "GBP"),
                        Amount(50.0, "RUB"),
                    ),
            )

        val fxRates =
            FxRates(
                base = "USD",
                rates =
                    mapOf(
                        "RUB" to 100.0,
                        "GBP" to 1.6,
                    ),
            )

        val newBalance = useCase.recalculate(balance, fxRates, "USD")
        val partAmount = newBalance.participantsBalance.first().amounts
        // 50 / 100 + 40 / 1.6 + 20 = 45.5
        partAmount.size shouldBe 2
        partAmount.find { it.currencyCode == "USD" }?.value shouldBe 45.5
        partAmount.find { it.currencyCode == "EUR" }?.value shouldBe 30.0
    }

    @Test
    fun `map_currency_to_non_base_value`() {
        val useCase = BalanceFxCalculationUseCase()
        val balance =
            Balance(
                participantsBalance =
                    setOf(
                        ParticipantBalance(
                            participant = Participant("1", "a"),
                            amounts =
                                setOf(
                                    Amount(20.0, "USD"),
                                    Amount(30.0, "EUR"),
                                    Amount(40.0, "GBP"),
                                    Amount(50.0, "RUB"),
                                ),
                        ),
                    ),
                undistributed =
                    setOf(
                        Amount(20.0, "USD"),
                        Amount(30.0, "EUR"),
                        Amount(40.0, "GBP"),
                        Amount(50.0, "RUB"),
                    ),
            )

        val fxRates =
            FxRates(
                base = "USD",
                rates =
                    mapOf(
                        "RUB" to 100.0,
                        "GBP" to 1.6,
                    ),
            )

        val newBalance = useCase.recalculate(balance, fxRates, "RUB")
        val partAmount = newBalance.participantsBalance.first().amounts
        // 20 * 100 + 40 / 1.6 * 100 + 50
        partAmount.size shouldBe 2
        partAmount.find { it.currencyCode == "RUB" }?.value shouldBe 4550.0
        partAmount.find { it.currencyCode == "EUR" }?.value shouldBe 30.0
    }
}
