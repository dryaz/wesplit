package app.wesplit.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import app.wesplit.theme.extraColorScheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.add_group_image
import split.composeapp.generated.resources.category_magic
import split.composeapp.generated.resources.ic_cat_magic
import split.composeapp.generated.resources.ic_eifell_ads
import split.composeapp.generated.resources.ic_plus
import split.composeapp.generated.resources.plus_badge
import split.composeapp.generated.resources.plus_feature_cats_cta
import split.composeapp.generated.resources.plus_feature_cats_title
import split.composeapp.generated.resources.plus_feature_images_descr_short
import split.composeapp.generated.resources.plus_feature_images_title

enum class Banner {
    AI_CAT,
    IMG_GROUP,
    QUICK_ADD,
}

@Composable
fun Banner.title() =
    when (this) {
        Banner.AI_CAT -> stringResource(Res.string.plus_feature_cats_title)
        Banner.IMG_GROUP -> stringResource(Res.string.plus_feature_images_title)
        Banner.QUICK_ADD -> TODO("Yet not showing this type of banner")
    }

@Composable
fun Banner.cta() =
    when (this) {
        Banner.AI_CAT -> stringResource(Res.string.plus_feature_cats_cta)
        Banner.IMG_GROUP -> stringResource(Res.string.plus_feature_images_descr_short)
        Banner.QUICK_ADD -> TODO("Yet not showing this type of banner")
    }

@Composable
fun Banner.icon() =
    when (this) {
        Banner.AI_CAT ->
            Column {
                Icon(
                    modifier = Modifier.width(56.dp),
                    painter = painterResource(Res.drawable.ic_cat_magic),
                    contentDescription = stringResource(Res.string.category_magic),
                )
                Spacer(modifier = Modifier.height(6.dp))
                Image(
                    modifier = Modifier.height(16.dp),
                    painter = painterResource(Res.drawable.ic_plus),
                    contentDescription = stringResource(Res.string.plus_badge),
                )
            }

        Banner.IMG_GROUP ->
            Box(
                modifier = Modifier.fillMaxHeight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    modifier = Modifier.width(48.dp).clip(CircleShape).background(MaterialTheme.extraColorScheme.onInfoContainer),
                    painter = painterResource(Res.drawable.ic_eifell_ads),
                    contentDescription = stringResource(Res.string.add_group_image),
                )
            }

        Banner.QUICK_ADD -> TODO("Yet not showing this type of banner")
    }

@Composable
fun FeatureBanner(
    banner: Banner,
    onClick: (Banner) -> Unit,
) {
    ListItem(
        modifier =
            Modifier.clickable {
                onClick(banner)
            },
        headlineContent = {
            Text(banner.title())
        },
        supportingContent = {
            Text(
                text = banner.cta(),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        colors =
            ListItemDefaults.colors(
                containerColor = MaterialTheme.extraColorScheme.infoContainer,
                supportingColor = MaterialTheme.extraColorScheme.onInfoContainer,
                leadingIconColor = MaterialTheme.extraColorScheme.onInfoContainer,
                headlineColor = MaterialTheme.extraColorScheme.onInfoContainer,
            ),
        leadingContent = {
            banner.icon()
        },
    )
}
