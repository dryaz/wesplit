package app.wesplit.paywall

import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.img_feature_protect
import split.composeapp.generated.resources.plus_feature_currencies_descr_full
import split.composeapp.generated.resources.plus_feature_currencies_descr_short
import split.composeapp.generated.resources.plus_feature_currencies_title
import split.composeapp.generated.resources.plus_feature_protect_descr_full
import split.composeapp.generated.resources.plus_feature_protect_descr_short
import split.composeapp.generated.resources.plus_feature_protect_title

internal val features: List<Feature> =
    listOf(
        Feature(
            title = Res.string.plus_feature_protect_title,
            shortDescr = Res.string.plus_feature_protect_descr_short,
            fullDescr = Res.string.plus_feature_protect_descr_full,
            image = Res.drawable.img_feature_protect,
        ),
        Feature(
            title = Res.string.plus_feature_currencies_title,
            shortDescr = Res.string.plus_feature_currencies_descr_short,
            fullDescr = Res.string.plus_feature_currencies_descr_full,
            image = Res.drawable.img_feature_protect,
        ),
    )

internal data class Feature(
    val title: StringResource,
    val shortDescr: StringResource,
    val fullDescr: StringResource,
    val image: DrawableResource,
)
