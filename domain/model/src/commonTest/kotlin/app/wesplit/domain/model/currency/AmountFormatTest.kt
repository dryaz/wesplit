package app.wesplit.domain.model.currency

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class AmountFormatTest {
    @Test
    fun format_positive_with_currency() {
        Amount(5.321123, "USD").format() shouldBe "$5.32"
    }

    @Test
    fun format_positive_without_currency() {
        Amount(5.321123, "USD").format(false) shouldBe "5.32"
    }

    @Test
    fun format_negative_with_currency() {
        Amount(-5.321123, "USD").format() shouldBe "-$5.32"
    }

    @Test
    fun format_negative_without_currency() {
        Amount(-5.321123, "USD").format(false) shouldBe "-5.32"
    }
}
