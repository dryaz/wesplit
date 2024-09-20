package app.wesplit.routing

import com.motorro.keeplink.deeplink.DeepLinkSerializer
import com.motorro.keeplink.deeplink.LinkBuilder
import com.motorro.keeplink.deeplink.LinkParser
import com.motorro.keeplink.deeplink.SchemeHostLinkBuilder
import com.motorro.keeplink.deeplink.SchemeHostLinkParser
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

object DeeplinkParsers {
    val LOCALHOST_8080: LinkParser<DeeplinkAction> = SchemeHostLinkParser(RootActionParser, "http", "localhost:8080")
    val PROD: LinkParser<DeeplinkAction> = SchemeHostLinkParser(RootActionParser, "https", "wesplit.app")
}

@JsExport
@OptIn(ExperimentalJsExport::class)
object DeeplinkBuilders {
    val LOCALHOST_8080: LinkBuilder<DeeplinkAction> = SchemeHostLinkBuilder("http", "localhost:8080")
    val PROD: LinkBuilder<DeeplinkAction> = SchemeHostLinkBuilder("https", "wesplit.app")
}

object DeeplinkSerializers {
    val LOCALHOST_8080 = DeepLinkSerializer(DeeplinkBuilders.LOCALHOST_8080, DeeplinkParsers.LOCALHOST_8080)
    val PROD = DeepLinkSerializer(DeeplinkBuilders.PROD, DeeplinkParsers.PROD)
}
