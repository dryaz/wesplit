package app.wesplit.routing

import com.motorro.keeplink.deeplink.deepLink
import com.motorro.keeplink.uri.data.utm
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
    fun test_prod_deeplink_with_token() {
        val link = "https://web.wesplit.app/group/1q2w3e4r?token=abc123"
        val parsed = DeeplinkParsers.PROD.parse(link)
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

    @Test
    fun gropup_share_link_encoder() {
        val action = DeeplinkAction.GroupDetails("abc", "123")
        val link = deepLink(action).withUtm(utm("app"))
        val groupDetailsUrl = DeeplinkBuilders.PROD.build(link)
        groupDetailsUrl shouldBe "https://web.wesplit.app/group/abc?token=123&utm_source=app"
    }
}
