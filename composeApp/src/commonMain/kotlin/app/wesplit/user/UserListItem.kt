package app.wesplit.user

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.user.User
import com.seiko.imageloader.model.ImageAction
import com.seiko.imageloader.rememberImageSuccessPainter
import com.seiko.imageloader.ui.AutoSizeBox
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.ic_user
import split.composeapp.generated.resources.you

@Composable
fun UserListItem(
    user: User,
    action: @Composable (() -> Unit)? = null,
    onClick: ((User) -> Unit)? = null,
) {
    val modifier = onClick?.let { Modifier.clickable { onClick(user) } } ?: Modifier
    Row(
        modifier =
            modifier
                .fillMaxWidth(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AutoSizeBox(
            url = user.photoUrl ?: "",
        ) { action ->
            when (action) {
                is ImageAction.Success -> {
                    Image(
                        rememberImageSuccessPainter(action),
                        modifier = Modifier.size(56.dp).clip(CircleShape),
                        contentDescription = user.name,
                    )
                }

                is ImageAction.Loading -> {
                    CircularProgressIndicator()
                }

                is ImageAction.Failure -> {
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerLow),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(Res.drawable.ic_user),
                            contentDescription = "No image for user ${user.name}",
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))
        Text(
            modifier = Modifier.weight(1f),
            text = user.name + if (user.isCurrentUser) " (${stringResource(Res.string.you)})" else "",
        )
        action?.let {
            it()
        }
    }
}
