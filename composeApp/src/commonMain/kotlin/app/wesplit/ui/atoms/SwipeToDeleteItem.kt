package app.wesplit.ui.atoms

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.KotlinPlatform
import app.wesplit.domain.model.currentPlatform
import io.github.alexzhirkevich.cupertino.adaptive.icons.AdaptiveIcons
import io.github.alexzhirkevich.cupertino.adaptive.icons.Delete
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.delete_expense

@Composable
fun SwipeToDeleteItem(
    modifier: Modifier = Modifier,
    onDelete: () -> Unit,
    content: @Composable () -> Unit,
) {
    val offsetX = remember { Animatable(0f) }
    val maxOffset = 300f // Adjust based on delete button width
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier.height(IntrinsicSize.Max),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .clickable {
                        coroutineScope.launch {
                            offsetX.animateTo(0f)
                        }

                        onDelete()
                    }
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(end = 24.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Icon(
                modifier = Modifier.fillMaxHeight(),
                imageVector = AdaptiveIcons.Outlined.Delete,
                contentDescription = stringResource(Res.string.delete_expense),
                tint = MaterialTheme.colorScheme.onErrorContainer,
            )
        }

        // Main content
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .offset { IntOffset(offsetX.value.toInt(), 0) }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { _, dragAmount ->
                                coroutineScope.launch {
                                    val targetValue = offsetX.value + dragAmount
                                    offsetX.snapTo(targetValue.coerceIn(-maxOffset, 0f))
                                }
                            },
                            onDragEnd = {
                                coroutineScope.launch {
                                    if (offsetX.value < -maxOffset / 2) {
                                        offsetX.animateTo(-maxOffset)
                                    } else {
                                        offsetX.animateTo(0f)
                                    }
                                }
                            },
                        )
                    }.then(
                        if (currentPlatform !is KotlinPlatform.Mobile) {
                            Modifier.clickable {
                                coroutineScope.launch {
                                    // Reveal delete button on click
                                    if (offsetX.value == 0f) {
                                        offsetX.animateTo(-maxOffset)
                                    } else {
                                        offsetX.animateTo(0f)
                                    }
                                }
                            }
                        } else {
                            Modifier
                        },
                    ),
        ) {
            content()
        }
    }
}
