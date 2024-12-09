package app.wesplit.group

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.group.Group
import app.wesplit.routing.DeeplinkAction
import app.wesplit.routing.DeeplinkBuilders
import app.wesplit.theme.extraColorScheme
import app.wesplit.ui.molecules.GroupHead
import com.motorro.keeplink.deeplink.deepLink
import io.github.alexzhirkevich.qrose.options.QrBallShape
import io.github.alexzhirkevich.qrose.options.QrBrush
import io.github.alexzhirkevich.qrose.options.QrFrameShape
import io.github.alexzhirkevich.qrose.options.QrLogoPadding
import io.github.alexzhirkevich.qrose.options.QrLogoShape
import io.github.alexzhirkevich.qrose.options.QrPixelShape
import io.github.alexzhirkevich.qrose.options.brush
import io.github.alexzhirkevich.qrose.options.circle
import io.github.alexzhirkevich.qrose.options.roundCorners
import io.github.alexzhirkevich.qrose.options.solid
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.img_logo_color
import split.composeapp.generated.resources.share_group

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ShareQrDialog(
    group: Group,
    isFullScreen: Boolean = false,
    onClose: () -> Unit,
    onShare: (String) -> Unit,
) {
    val detailsAction =
        DeeplinkAction.Group.Details(
            groupId = group.id,
            token = group.publicToken,
        )
    val link = deepLink(detailsAction)
    val groupDetailsUrl = DeeplinkBuilders.PROD.build(link)

    val sheetState: SheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = isFullScreen,
        )

    val logoPainter: Painter = painterResource(Res.drawable.img_logo_color)
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface

    ModalBottomSheet(
        modifier = Modifier,
        sheetState = sheetState,
        onDismissRequest = { onClose() },
    ) {
        Column(
            modifier = Modifier.wrapContentHeight().fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            GroupHead(group = group)
            Spacer(modifier = Modifier.height(16.dp))
            Image(
                modifier = Modifier.height(250.dp).fillMaxWidth(),
                painter =
                    rememberQrCodePainter(groupDetailsUrl) {
                        logo {
                            painter = logoPainter
                            padding = QrLogoPadding.Natural(.1f)
                            shape = QrLogoShape.circle()
                            size = 0.2f
                        }

                        shapes {
                            ball = QrBallShape.circle()
                            darkPixel = QrPixelShape.roundCorners()
                            frame = QrFrameShape.roundCorners(.25f)
                        }
                        colors {
                            light = QrBrush.solid(surface)
                            dark =
                                QrBrush.brush {
                                    Brush.linearGradient(
                                        0f to primaryColor,
                                        1f to primaryContainerColor,
                                        end = Offset(it, it),
                                    )
                                }
                            frame = QrBrush.solid(onSurface)
                        }
                    },
                contentDescription = stringResource(Res.string.share_group),
            )
            Spacer(modifier = Modifier.height(32.dp))
            FilledTonalButton(
                modifier = Modifier,
                onClick = { onShare("") },
                shape = RoundedCornerShape(10.dp),
                colors =
                    ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.extraColorScheme.infoContainer,
                        contentColor = MaterialTheme.extraColorScheme.onInfoContainer,
                    ),
            ) {
                Text(stringResource(Res.string.share_group))
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
