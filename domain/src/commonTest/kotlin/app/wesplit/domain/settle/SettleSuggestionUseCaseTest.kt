package app.wesplit.domain.settle

import app.wesplit.domain.model.currency.Amount
import app.wesplit.domain.model.group.Balance
import app.wesplit.domain.model.group.Participant
import app.wesplit.domain.model.group.ParticipantBalance
import io.kotest.matchers.shouldBe
import kotlin.math.absoluteValue
import kotlin.test.Test

class SettleSuggestionUseCaseTest {
    @Test
    fun reproduce_mark_bug_fxed_perons_balance_should_match() {
        val useCase = SettleSuggestionUseCase()
        val p1 = Participant("1", "Moshkov")
        val p2 = Participant("2", "Rofin")
        val p3 = Participant("3", "Gitman")
        val p4 = Participant("4", "Raevskaja")
        val p5 = Participant("5", "Kurchenko")
        val p6 = Participant("6", "Bakalova")
        val p7 = Participant("7", "Makojan")
        val p8 = Participant("8", "Katunkin")
        val p9 = Participant("9", "Baturin")
        val p10 = Participant("10", "Silevich")
        val p11 = Participant("11", "Sosnin")
        val p12 = Participant("12", "Grisha")
        val p13 = Participant("13", "Sidorov")
        val p14 = Participant("14", "Shitov")
        val p15 = Participant("15", "Lera")
        val p16 = Participant("16", "Malkov")

        val balance =
            Balance(
                participantsBalance =
                    setOf(
                        ParticipantBalance(
                            participant = p1,
                            amounts =
                                setOf(
                                    Amount(17.80, "EUR"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p2,
                            amounts =
                                setOf(
                                    Amount(1430.81, "EUR"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p3,
                            amounts =
                                setOf(
                                    Amount(-217.98, "EUR"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p4,
                            amounts =
                                setOf(
                                    Amount(-450.30, "EUR"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p5,
                            amounts =
                                setOf(
                                    Amount(-465.77, "EUR"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p6,
                            amounts =
                                setOf(
                                    Amount(1073.39, "EUR"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p7,
                            amounts =
                                setOf(
                                    Amount(-94.65, "EUR"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p8,
                            amounts =
                                setOf(
                                    Amount(-171.89, "EUR"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p9,
                            amounts =
                                setOf(
                                    Amount(477.98, "EUR"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p10,
                            amounts =
                                setOf(
                                    Amount(-337.69, "EUR"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p11,
                            amounts =
                                setOf(
                                    Amount(-362.42, "EUR"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p12,
                            amounts =
                                setOf(
                                    Amount(-179.85, "EUR"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p13,
                            amounts =
                                setOf(
                                    Amount(-179.85, "EUR"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p14,
                            amounts =
                                setOf(
                                    Amount(-179.85, "EUR"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p15,
                            amounts =
                                setOf(
                                    Amount(-179.85, "EUR"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p16,
                            amounts =
                                setOf(
                                    Amount(-179.85, "EUR"),
                                ),
                        ),
                    ),
            )

        val result =
            useCase.get(balance)
                .filterNot { it.payer == null }
                .groupingBy { it.payer!! }
                .fold(0.0) { acc, suggestion -> acc + suggestion.amount.value }

        result.forEach { computed ->
            println("Check balance for ${computed.key.name}")
            computed.value.absoluteValue shouldBe
                balance.participantsBalance.first {
                    it.participant == computed.key
                }.amounts.first().value.absoluteValue
        }
    }

    @Test
    fun reproduce_bug() {
        val useCase = SettleSuggestionUseCase()
        val p1 = Participant("1", "Alice")
        val p2 = Participant("2", "Bob")
        val p3 = Participant("3", "Charlie")
        val p4 = Participant("4", "Charlie")
        val p5 = Participant("5", "Charlie")
        val p6 = Participant("6", "Charlie")
        val p7 = Participant("7", "Charlie")
        val p8 = Participant("8", "Charlie")

        val balance =
            Balance(
                participantsBalance =
                    setOf(
                        ParticipantBalance(
                            participant = p1,
                            amounts =
                                setOf(
                                    Amount(-144310.32, "AMD"),
                                    Amount(1113.56, "GEL"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p2,
                            amounts =
                                setOf(
                                    Amount(-144310.32, "AMD"),
                                    Amount(-12.76, "GEL"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p3,
                            amounts =
                                setOf(
                                    Amount(438752.66, "AMD"),
                                    Amount(188.03, "GEL"),
                                    Amount(-460.0, "USD"),
                                    Amount(-13550.0, "RUB"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p4,
                            amounts =
                                setOf(
                                    Amount(-538.75, "AMD"),
                                    Amount(-165.09, "GEL"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p5,
                            amounts =
                                setOf(
                                    Amount(-142477.0, "AMD"),
                                    Amount(-126.76, "GEL"),
                                    Amount(460.0, "USD"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p6,
                            amounts =
                                setOf(
                                    Amount(-2372.07, "AMD"),
                                    Amount(-312.76, "GEL"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p7,
                            amounts =
                                setOf(
                                    Amount(-2372.07, "AMD"),
                                    Amount(-311.76, "GEL"),
                                    Amount(13550.0, "RUB"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p8,
                            amounts =
                                setOf(
                                    Amount(-2372.07, "AMD"),
                                    Amount(-372.43, "GEL"),
                                ),
                        ),
                    ),
            )

        useCase.get(balance)
        // Expect NO crash
    }

    @Test
    fun messed_i_j_case_trigger_condition() {
        val useCase = SettleSuggestionUseCase()
        val p1 = Participant("1", "Alice")
        val p2 = Participant("2", "Bob")
        val p3 = Participant("3", "Charlie")

        val balance =
            Balance(
                participantsBalance =
                    setOf(
                        ParticipantBalance(
                            participant = p1,
                            amounts =
                                setOf(
                                    Amount(-30.0, "USD"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p2,
                            amounts =
                                setOf(
                                    Amount(-30.0, "EUR"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p3,
                            amounts =
                                setOf(
                                    Amount(30.0, "USD"),
                                    Amount(30.0, "EUR"),
                                ),
                        ),
                    ),
            )

        val suggestions = useCase.get(balance)

        // Expect 2 settlements
        suggestions.size shouldBe 2

        // Verify the first settlement
        suggestions.first { it.payer == p1 }.let {
            it.payer shouldBe p1
            it.recipient shouldBe p3
            it.amount shouldBe Amount(30.0, "USD")
        }

        // Verify the second settlement
        suggestions.first { it.payer == p2 }.let {
            it.payer shouldBe p2
            it.recipient shouldBe p3
            it.amount shouldBe Amount(30.0, "EUR")
        }
    }

    @Test
    fun messed_i_j_case_typo_specific() {
        val useCase = SettleSuggestionUseCase()
        val p1 = Participant("1", "Alice")
        val p2 = Participant("2", "Bob")
        val p3 = Participant("3", "Charlie")
        val p4 = Participant("4", "David")

        // Setup balances where creditor indices differ
        val balance =
            Balance(
                participantsBalance =
                    setOf(
                        ParticipantBalance(
                            participant = p1,
                            amounts =
                                setOf(
                                    Amount(-40.0, "USD"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p2,
                            amounts =
                                setOf(
                                    Amount(-20.0, "USD"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p3,
                            amounts =
                                setOf(
                                    Amount(30.0, "USD"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p4,
                            amounts =
                                setOf(
                                    Amount(30.0, "USD"),
                                ),
                        ),
                    ),
            )

        val suggestions = useCase.get(balance)

        // Check that we settle p1 with p3 and p4 correctly
        suggestions.size shouldBe 3

        // Ensure the first settlement is correct
        suggestions.first { it.payer == p1 && it.recipient == p3 }.let {
            it.payer shouldBe p1
            it.recipient shouldBe p3
            it.amount shouldBe Amount(30.0, "USD")
        }

        // Ensure the second settlement is correct
        suggestions.first { it.payer == p1 && it.recipient == p4 }.let {
            it.payer shouldBe p1
            it.recipient shouldBe p4
            it.amount shouldBe Amount(10.0, "USD")
        }

        // Ensure the third settlement is correct
        suggestions.first { it.payer == p2 }.let {
            it.payer shouldBe p2
            it.recipient shouldBe p4
            it.amount shouldBe Amount(20.0, "USD")
        }
    }

    @Test
    fun messed_i_j_case_typo() {
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
                                    Amount(-30.0, "USD"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p2,
                            amounts =
                                setOf(
                                    Amount(-20.0, "USD"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p3,
                            amounts =
                                setOf(
                                    Amount(50.0, "USD"),
                                ),
                        ),
                    ),
            )

        val suggestions = useCase.get(balance)

        // The correct result should be two suggestions:
        // 1. p1 pays p3 30.0 USD
        // 2. p2 pays p3 20.0 USD

        suggestions.size shouldBe 2

        // Ensure suggestion for p1 is correct
        suggestions.first { it.payer == p1 }.let {
            it.payer shouldBe p1
            it.recipient shouldBe p3
            it.amount shouldBe Amount(30.0, "USD")
        }

        // Ensure suggestion for p2 is correct
        suggestions.first { it.payer == p2 }.let {
            it.payer shouldBe p2
            it.recipient shouldBe p3
            it.amount shouldBe Amount(20.0, "USD")
        }
    }

    @Test
    fun messed_i_j_case_3() {
        val useCase = SettleSuggestionUseCase()
        val p1 = Participant("1", "a")
        val p2 = Participant("2", "b")
        val p3 = Participant("3", "c")
        val balance =
            Balance(
                participantsBalance =
                    setOf(
                        ParticipantBalance(
                            participant = p2,
                            amounts =
                                setOf(
                                    Amount(-30.0, "USD"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p1,
                            amounts =
                                setOf(
                                    Amount(-20.0, "USD"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p3,
                            amounts =
                                setOf(
                                    Amount(50.0, "USD"),
                                ),
                        ),
                    ),
            )

        val suggestions = useCase.get(balance)
        suggestions.size shouldBe 2
        suggestions.first { it.payer == p1 }.let {
            it.payer shouldBe p1
            it.recipient shouldBe p3
            it.amount shouldBe Amount(20.0, "USD")
        }

        suggestions.first { it.payer == p2 }.let {
            it.payer shouldBe p2
            it.recipient shouldBe p3
            it.amount shouldBe Amount(30.0, "USD")
        }
    }

    @Test
    fun messed_i_j_case_2() {
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
                                    Amount(-20.0, "USD"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p2,
                            amounts =
                                setOf(
                                    Amount(-30.0, "EUR"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p3,
                            amounts =
                                setOf(
                                    Amount(20.0, "USD"),
                                    Amount(30.0, "EUR"),
                                ),
                        ),
                    ),
            )

        val suggestions = useCase.get(balance)
        suggestions.size shouldBe 2
        suggestions.first { it.payer == p1 }.let {
            it.payer shouldBe p1
            it.recipient shouldBe p3
            it.amount shouldBe Amount(20.0, "USD")
        }

        suggestions.first { it.payer == p2 }.let {
            it.payer shouldBe p2
            it.recipient shouldBe p3
            it.amount shouldBe Amount(30.0, "EUR")
        }
    }

    @Test
    fun messed_i_j_case() {
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
                                    Amount(30.0, "EUR"),
                                ),
                        ),
                        ParticipantBalance(
                            participant = p3,
                            amounts =
                                setOf(
                                    Amount(-20.0, "USD"),
                                    Amount(-30.0, "EUR"),
                                ),
                        ),
                    ),
            )

        val suggestions = useCase.get(balance)
        suggestions.size shouldBe 2
        suggestions.first { it.payer == p3 }.let {
            it.payer shouldBe p3
            it.recipient shouldBe p1
            it.amount shouldBe Amount(20.0, "USD")
        }

        suggestions.last { it.payer == p3 }.let {
            it.payer shouldBe p3
            it.recipient shouldBe p2
            it.amount shouldBe Amount(30.0, "EUR")
        }
    }

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
