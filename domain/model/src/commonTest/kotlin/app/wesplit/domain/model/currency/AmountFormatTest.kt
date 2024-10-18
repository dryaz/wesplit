package app.wesplit.domain.model.currency

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class AmountFormatTest {
    @Test
    fun format_positive_with_currency() {
        Amount(5.321123, "USD").format() shouldBe "$5,32"
    }

    @Test
    fun format_positive_without_currency() {
        Amount(5.321123, "USD").format(false) shouldBe "5,32"
    }

    @Test
    fun format_negative_with_currency() {
        Amount(-5.321123, "USD").format() shouldBe "-$5,32"
    }

    @Test
    fun format_negative_without_currency() {
        Amount(-5.321123, "USD").format(false) shouldBe "-5,32"
    }

    @Test
    fun format_positive_big_with_currency() {
        Amount(5123321.321123, "USD").format() shouldBe "$5.123.321,32"
    }

    @Test
    fun format_positive_big_without_currency() {
        Amount(51233211.321123, "USD").format(false) shouldBe "51.233.211,32"
    }

    @Test
    fun format_negative_big_with_currency() {
        Amount(-51233211.321123, "USD").format() shouldBe "-$51.233.211,32"
    }

    @Test
    fun format_negative_big_without_currency() {
        Amount(-51233211.321123, "USD").format(false) shouldBe "-51.233.211,32"
    }
}
