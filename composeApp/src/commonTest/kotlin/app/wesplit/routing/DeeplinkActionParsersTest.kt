package app.wesplit.routing

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

class DeeplinkActionParsersTest {
    @Test
    fun test_profile_deeplink() {
        val link = "http://localhost:8080/profile"
        val parsed = DeeplinkParsers.LOCALHOST_8080.parse(link)
        parsed?.action?.shouldBeInstanceOf<DeeplinkAction.Profile>()
    }

    @Test
    fun test_group_deeplink() {
        val link = "http://localhost:8080/group/1q2w3e4r"
        val parsed = DeeplinkParsers.LOCALHOST_8080.parse(link)
        parsed?.action?.shouldBeInstanceOf<DeeplinkAction.GroupDetails>()
        (parsed!!.action as? DeeplinkAction.GroupDetails)?.let {
            it.groupId shouldBe "1q2w3e4r"
            it.token shouldBe null
        }
    }

    @Test
    fun test_group_deeplink_with_token() {
        val link = "http://localhost:8080/group/1q2w3e4r?token=abc123"
        val parsed = DeeplinkParsers.LOCALHOST_8080.parse(link)
        parsed?.action?.shouldBeInstanceOf<DeeplinkAction.GroupDetails>()
        (parsed!!.action as? DeeplinkAction.GroupDetails)?.let {
            it.groupId shouldBe "1q2w3e4r"
            it.token shouldBe "abc123"
        }
    }

    @Test
    fun empty_link_parse() {
        val link = ""
        val parsed = DeeplinkParsers.LOCALHOST_8080.parse(link)
        parsed shouldBe null
    }
}
