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
        parsed?.action?.shouldBeInstanceOf<DeeplinkAction.Group.Details>()
        (parsed!!.action as? DeeplinkAction.Group.Details)?.let {
            it.groupId shouldBe "1q2w3e4r"
            it.token shouldBe null
        }
    }

    @Test
    fun test_group_deeplink_with_token() {
        val link = "http://localhost:8080/group/1q2w3e4r?token=abc123"
        val parsed = DeeplinkParsers.LOCALHOST_8080.parse(link)
        parsed?.action?.shouldBeInstanceOf<DeeplinkAction.Group.Details>()
        (parsed!!.action as? DeeplinkAction.Group.Details)?.let {
            it.groupId shouldBe "1q2w3e4r"
            it.token shouldBe "abc123"
        }
    }

    @Test
    fun test_prod_deeplink_with_token() {
        val link = "https://web.wesplit.app/group/1q2w3e4r?token=abc123"
        val parsed = DeeplinkParsers.PROD.parse(link)
        parsed?.action?.shouldBeInstanceOf<DeeplinkAction.Group.Details>()
        (parsed!!.action as? DeeplinkAction.Group.Details)?.let {
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
        val action = DeeplinkAction.Group.Details("abc", "123")
        val link = deepLink(action).withUtm(utm("app"))
        val groupDetailsUrl = DeeplinkBuilders.PROD.build(link)
        groupDetailsUrl shouldBe "https://web.wesplit.app/group/abc?token=123&utm_source=app"
    }

    @Test
    fun gropup_add_expense_link_encoder() {
        val action = DeeplinkAction.Group.Expense("abc")
        val link = deepLink(action)
        val groupDetailsUrl = DeeplinkBuilders.PROD.build(link)
        groupDetailsUrl shouldBe "https://web.wesplit.app/group/abc/expense/"
    }

    @Test
    fun expense_details_link_encoder() {
        val action = DeeplinkAction.Group.Expense("abc", "123")
        val link = deepLink(action)
        val groupDetailsUrl = DeeplinkBuilders.PROD.build(link)
        groupDetailsUrl shouldBe "https://web.wesplit.app/group/abc/expense/123"
    }

    @Test
    fun expense_details_link_decoder() {
        val link = "https://web.wesplit.app/group/abc/expense/123"
        val parsed = DeeplinkParsers.PROD.parse(link)
        parsed?.action?.shouldBeInstanceOf<DeeplinkAction.Group.Expense>()
        (parsed!!.action as? DeeplinkAction.Group.Expense)?.let {
            it.groupId shouldBe "abc"
            it.expenseId shouldBe "123"
        }
    }

    @Test
    fun gropup_add_expense_link_dencoder() {
        val link = "https://web.wesplit.app/group/abc/expense/"
        val parsed = DeeplinkParsers.PROD.parse(link)
        parsed?.action?.shouldBeInstanceOf<DeeplinkAction.Group.Expense>()
        (parsed!!.action as? DeeplinkAction.Group.Expense)?.let {
            it.groupId shouldBe "abc"
            it.expenseId shouldBe ""
        }
    }

    @Test
    fun gropup_add_expense_link_with_splash_not_known() {
        val link = "https://web.wesplit.app/group/abc/expense"
        val parsed = DeeplinkParsers.PROD.parse(link)
        parsed?.action?.shouldBeInstanceOf<DeeplinkAction.Unknown>()
    }
}
