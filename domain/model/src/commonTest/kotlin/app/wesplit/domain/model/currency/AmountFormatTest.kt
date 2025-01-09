package app.wesplit.domain.model.currency

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class AmountFormatTest {
    @Test
    fun format_14_59999999_test() {
        Amount(14.599999999, "USD").format() shouldBe "$14,60"
    }

    @Test
    fun format_14_51111111_test() {
        Amount(14.51111111, "USD").format() shouldBe "$14,51"
    }

    @Test
    fun format_14_6_issue() {
        Amount(14.6, "USD").format() shouldBe "$14,60"
    }

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
