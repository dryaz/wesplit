package app.wesplit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import app.wesplit.domain.model.user.OnboardingStep
import app.wesplit.theme.extraColorScheme
import io.github.alexzhirkevich.cupertino.adaptive.icons.AdaptiveIcons
import io.github.alexzhirkevich.cupertino.adaptive.icons.Info
import kotlin.math.roundToInt

data class TutorialStep(
    val title: String,
    val description: String,
    val onboardingStep: OnboardingStep,
    val isModal: Boolean = true,
    val helpOverlayPosition: HelpOverlayPosition = HelpOverlayPosition.BOTTOM_RIGHT,
)

class TutorialControl(
    val stepRequest: (List<TutorialStep>) -> Unit,
    val onPositionRecieved: (TutorialStep, Rect) -> Unit,
    val onNext: () -> Unit,
)

val LocalTutorialControl = compositionLocalOf { TutorialControl({}, { _, _ -> }, {}) }

@Composable
fun TutorialItem(
    modifier: Modifier = Modifier,
    onPositioned: (Rect) -> Unit,
    isGlobalLayout: Boolean = true,
    content: @Composable (Modifier) -> Unit,
) {
    content(
        modifier.then(
            Modifier.onGloballyPositioned { layoutCoordinates ->
                val position = if (isGlobalLayout) layoutCoordinates.positionInRoot() else layoutCoordinates.positionInParent()
                val size = layoutCoordinates.size
                val rect =
                    Rect(
                        left = position.x,
                        top = position.y,
                        right = position.x + size.width,
                        bottom = position.y + size.height,
                    )
                onPositioned(rect)
            },
        ),
    )
}

@Composable
fun TutorialOverlay(
    targetBounds: Rect?,
    step: TutorialStep,
    helpOverlayPosition: HelpOverlayPosition = HelpOverlayPosition.BOTTOM_RIGHT,
    onClose: () -> Unit,
) {
    val highlightColor = MaterialTheme.extraColorScheme.infoContainer

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .drawBehind {
                    // Define the path with the entire screen and the hole
                    val overlayPath =
                        Path().apply {
                            // Add the entire screen as a path
                            addRect(Rect(0f, 0f, size.width, size.height))
                            // Add the hole (target area)
                            targetBounds?.let { bounds ->
                                addRoundRect(
                                    RoundRect(
                                        rect = bounds,
                                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                                    ),
                                )
                            }
                            // Use EvenOdd to create a hole
                            fillType = PathFillType.EvenOdd
                        }

                    // Draw the semi-transparent overlay with the hole
                    drawPath(
                        path = overlayPath,
                        color = Color.Black.copy(alpha = 0.55f),
                        style = Fill,
                    )

                    targetBounds?.let { bounds ->
                        // Define the path for the stroke around the hole
                        val strokePath =
                            Path().apply {
                                addRoundRect(
                                    RoundRect(
                                        rect = bounds,
                                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                                    ),
                                )
                                fillType = PathFillType.EvenOdd
                            }

                        // Draw the stroke around the hole
                        drawPath(
                            path = strokePath,
                            color = highlightColor,
                            style = Stroke(width = 4.dp.toPx()),
                        )
                    }
                }.then(
                    if (step.isModal) {
                        Modifier.clickable(enabled = false) {
                            // Prevent clicks from passing through
                        }
                    } else {
                        Modifier
                    },
                ).zIndex(100f),
    ) {
        targetBounds?.let { bounds ->
            // State to hold the measured width of the Box
            var boxSize by remember { mutableStateOf(IntSize(0, 0)) }

            val x =
                remember(boxSize, helpOverlayPosition) {
                    when (helpOverlayPosition) {
                        HelpOverlayPosition.TOP_LEFT,
                        HelpOverlayPosition.BOTTOM_LEFT,
                        -> (bounds.right - boxSize.width).roundToInt()
                        HelpOverlayPosition.TOP_RIGHT,
                        HelpOverlayPosition.BOTTOM_RIGHT,
                        -> bounds.left.roundToInt()
                    }
                }

            val y =
                remember(boxSize, helpOverlayPosition) {
                    when (helpOverlayPosition) {
                        HelpOverlayPosition.TOP_LEFT,
                        HelpOverlayPosition.TOP_RIGHT,
                        -> (bounds.top - boxSize.height - 16).roundToInt()
                        HelpOverlayPosition.BOTTOM_LEFT,
                        HelpOverlayPosition.BOTTOM_RIGHT,
                        -> (bounds.bottom + 16).roundToInt()
                    }
                }

            Box(
                modifier =
                    Modifier
                        .offset {
                            IntOffset(
                                x = x,
                                y = y,
                            )
                        }
                        .background(MaterialTheme.extraColorScheme.infoContainer, RoundedCornerShape(8.dp))
                        .onSizeChanged { coordinates ->
                            boxSize = coordinates
                        }
                        .padding(16.dp)
                        .widthIn(max = 220.dp),
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = AdaptiveIcons.Outlined.Info,
                            contentDescription = "Tutorial information",
                            tint = MaterialTheme.extraColorScheme.onInfoContainer,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = step.title,
                            color = MaterialTheme.extraColorScheme.onInfoContainer,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = step.description,
                        color = MaterialTheme.extraColorScheme.onInfoContainer,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    if (step.isModal) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            TextButton(onClick = onClose) {
                                Text(
                                    text = "Got it",
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class HelpOverlayPosition {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
}
