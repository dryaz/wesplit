package app.wesplit.routing

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

class DeeplinkActionParsersTest {
    @Test
    fun test_profile_deeplink() {
        val link = "http://localhost:8080/profile"
        val parser = DeeplinkParsers.LOCALHOST_8080.parse(link)
        parser?.action?.shouldBeInstanceOf<DeeplinkAction.Profile>()
    }

    @Test
    fun test_group_deeplink() {
        val link = "http://localhost:8080/group/1q2w3e4r"
        val parser = DeeplinkParsers.LOCALHOST_8080.parse(link)
        parser?.action?.shouldBeInstanceOf<DeeplinkAction.GroupDetails>()
        (parser!!.action as? DeeplinkAction.GroupDetails)?.let {
            it.groupId shouldBe "1q2w3e4r"
        }
    }
}
