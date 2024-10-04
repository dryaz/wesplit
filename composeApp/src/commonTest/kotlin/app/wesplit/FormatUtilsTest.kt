package app.wesplit

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class FormatUtilsTest {
    @Test
    fun mixed_string_formatted() {
        "1a2.3,4".filterDoubleInput() shouldBe "12.34"
    }

    @Test
    fun comma_to_dot_format() {
        "1,234".filterDoubleInput() shouldBe "1.23"
    }

    @Test
    fun doule_format() {
        "1.2".filterDoubleInput() shouldBe "1.2"
    }
}
