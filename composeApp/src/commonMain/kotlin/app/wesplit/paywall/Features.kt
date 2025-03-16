package app.wesplit.paywall

import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.img_feature_ai_group
import split.composeapp.generated.resources.img_feature_cat_arrow
import split.composeapp.generated.resources.img_feature_fx
import split.composeapp.generated.resources.img_feature_no_ads
import split.composeapp.generated.resources.img_feature_protect
import split.composeapp.generated.resources.img_feature_quick_add
import split.composeapp.generated.resources.img_feature_single_settle
import split.composeapp.generated.resources.plus_feature_cats_descr_full
import split.composeapp.generated.resources.plus_feature_cats_descr_short
import split.composeapp.generated.resources.plus_feature_cats_title
import split.composeapp.generated.resources.plus_feature_currencies_descr_full
import split.composeapp.generated.resources.plus_feature_currencies_descr_short
import split.composeapp.generated.resources.plus_feature_currencies_title
import split.composeapp.generated.resources.plus_feature_images_descr_full
import split.composeapp.generated.resources.plus_feature_images_descr_short
import split.composeapp.generated.resources.plus_feature_images_title
import split.composeapp.generated.resources.plus_feature_noads_descr_full
import split.composeapp.generated.resources.plus_feature_noads_descr_short
import split.composeapp.generated.resources.plus_feature_noads_title
import split.composeapp.generated.resources.plus_feature_protect_descr_full
import split.composeapp.generated.resources.plus_feature_protect_descr_short
import split.composeapp.generated.resources.plus_feature_protect_title
import split.composeapp.generated.resources.plus_feature_quick_add_descr_full
import split.composeapp.generated.resources.plus_feature_quick_add_descr_short
import split.composeapp.generated.resources.plus_feature_quick_add_title
import split.composeapp.generated.resources.plus_feature_single_settle_descr_full
import split.composeapp.generated.resources.plus_feature_single_settle_descr_short
import split.composeapp.generated.resources.plus_feature_single_settle_title

internal val features: List<Feature> =
    listOf(
        Feature(
            title = Res.string.plus_feature_cats_title,
            shortDescr = Res.string.plus_feature_cats_descr_short,
            fullDescr = Res.string.plus_feature_cats_descr_full,
            image = Res.drawable.img_feature_cat_arrow,
        ),
        Feature(
            title = Res.string.plus_feature_images_title,
            shortDescr = Res.string.plus_feature_images_descr_short,
            fullDescr = Res.string.plus_feature_images_descr_full,
            image = Res.drawable.img_feature_ai_group,
        ),
        Feature(
            title = Res.string.plus_feature_quick_add_title,
            shortDescr = Res.string.plus_feature_quick_add_descr_short,
            fullDescr = Res.string.plus_feature_quick_add_descr_full,
            image = Res.drawable.img_feature_quick_add,
        ),
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
            image = Res.drawable.img_feature_fx,
        ),
        Feature(
            title = Res.string.plus_feature_single_settle_title,
            shortDescr = Res.string.plus_feature_single_settle_descr_short,
            fullDescr = Res.string.plus_feature_single_settle_descr_full,
            image = Res.drawable.img_feature_single_settle,
        ),
        Feature(
            title = Res.string.plus_feature_noads_title,
            shortDescr = Res.string.plus_feature_noads_descr_short,
            fullDescr = Res.string.plus_feature_noads_descr_full,
            image = Res.drawable.img_feature_no_ads,
        ),
    )

internal data class Feature(
    val title: StringResource,
    val shortDescr: StringResource,
    val fullDescr: StringResource,
    val image: DrawableResource,
)
