package app.wesplit.domain.settle

import app.wesplit.domain.model.currency.Amount
import app.wesplit.domain.model.group.Balance
import app.wesplit.domain.model.group.Participant
import app.wesplit.domain.model.group.ParticipantBalance
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SettleSuggestionUseCaseTest {
    @Test
    fun simple_2_part_case() {
        val useCase = SettleSuggestionUseCase()
        val p1 = Participant("1", "a")
        val p2 = Participant("2", "b")
        val balance =
            Balance(
                participantsBalance =
                    setOf(
                        ParticipantBalance(
                            participant = p1,
                            amounts =
                                setOf(
                                    Amount(20.0, "USD"),
                                    Amount(-30.0, "EUR"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p2,
                            amounts =
                                setOf(
                                    Amount(-20.0, "USD"),
                                    Amount(30.0, "EUR"),
                                ),
                        ),
                    ),
            )

        val suggestions = useCase.get(balance)
        suggestions.size shouldBe 2
        suggestions.first { it.payer == p2 }.let {
            it.payer shouldBe p2
            it.recipient shouldBe p1
            it.amount shouldBe Amount(20.0, "USD")
        }

        suggestions.first { it.payer == p1 }.let {
            it.payer shouldBe p1
            it.recipient shouldBe p2
            it.amount shouldBe Amount(30.0, "EUR")
        }
    }

    @Test
    fun simple_3_part_case() {
        val useCase = SettleSuggestionUseCase()
        val p1 = Participant("1", "a")
        val p2 = Participant("2", "b")
        val p3 = Participant("3", "c")
        val balance =
            Balance(
                participantsBalance =
                    setOf(
                        ParticipantBalance(
                            participant = p1,
                            amounts =
                                setOf(
                                    Amount(20.0, "USD"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p2,
                            amounts =
                                setOf(
                                    Amount(-12.0, "USD"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p3,
                            amounts =
                                setOf(
                                    Amount(-8.0, "USD"),
                                ),
                        ),
                    ),
            )

        val suggestions = useCase.get(balance)
        suggestions.size shouldBe 2
        suggestions.first { it.payer == p2 }.let {
            it.payer shouldBe p2
            it.recipient shouldBe p1
            it.amount shouldBe Amount(12.0, "USD")
        }

        suggestions.first { it.payer == p3 }.let {
            it.payer shouldBe p3
            it.recipient shouldBe p1
            it.amount shouldBe Amount(8.0, "USD")
        }
    }

    @Test
    fun undistributed() {
        val useCase = SettleSuggestionUseCase()
        val p1 = Participant("1", "a")
        val p2 = Participant("2", "b")
        val balance =
            Balance(
                participantsBalance =
                    setOf(
                        ParticipantBalance(
                            participant = p1,
                            amounts =
                                setOf(
                                    Amount(20.0, "USD"),
                                ),
                        ),
                    ),
                undistributed =
                    setOf(
                        Amount(20.0, "USD"),
                    ),
            )

        val suggestions = useCase.get(balance)
        suggestions.size shouldBe 1
        suggestions.first { it.recipient == p1 }.let {
            it.recipient shouldBe p1
            it.payer shouldBe null
            it.amount shouldBe Amount(20.0, "USD")
        }
    }

    @Test
    fun combined() {
        val useCase = SettleSuggestionUseCase()
        val p1 = Participant("1", "a")
        val p2 = Participant("2", "b")
        val p3 = Participant("3", "c")
        val balance =
            Balance(
                participantsBalance =
                    setOf(
                        ParticipantBalance(
                            participant = p1,
                            amounts =
                                setOf(
                                    Amount(28.0, "USD"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p2,
                            amounts =
                                setOf(
                                    Amount(-12.0, "USD"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p3,
                            amounts =
                                setOf(
                                    Amount(-8.0, "USD"),
                                ),
                        ),
                    ),
                undistributed =
                    setOf(
                        Amount(8.0, "USD"),
                    ),
            )

        val suggestions = useCase.get(balance)
        suggestions.size shouldBe 3
        suggestions.first { it.payer == p2 }.let {
            it.payer shouldBe p2
            it.recipient shouldBe p1
            it.amount shouldBe Amount(12.0, "USD")
        }

        suggestions.first { it.payer == p3 }.let {
            it.payer shouldBe p3
            it.recipient shouldBe p1
            it.amount shouldBe Amount(8.0, "USD")
        }

        suggestions.first { it.payer == null }.let {
            it.recipient shouldBe p1
            it.payer shouldBe null
            it.amount shouldBe Amount(8.0, "USD")
        }
    }
}
