package app.wesplit.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.wesplit.theme.extraColorScheme
import io.github.alexzhirkevich.cupertino.adaptive.icons.AdaptiveIcons
import io.github.alexzhirkevich.cupertino.adaptive.icons.Done
import kotlin.math.roundToInt

data class TutorialStep(
    val description: String,
    val isModal: Boolean = true,
)

@Composable
fun TestTutorial() {
    var showTutorial by remember { mutableStateOf(true) }
    val steps =
        listOf(
            TutorialStep("First tutorial", isModal = false),
            TutorialStep("SecondTutorial", isModal = false),
            TutorialStep("ThirdTutorial"),
        )
    var currentStepIndex by remember { mutableStateOf(0) }
    val targetPositions = remember { mutableStateMapOf<TutorialStep, Rect>() }

    Column(
        modifier = Modifier.fillMaxSize(1f),
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        TutorialItem(
            modifier = Modifier.padding(start = 60.dp),
            step = steps[1],
            onPositioned = { tutorial, rect ->
                targetPositions[tutorial] = rect
            },
        ) { modifier ->
            FloatingActionButton(
                modifier = modifier,
                onClick = { currentStepIndex++ },
            ) {
                Icon(
                    AdaptiveIcons.Outlined.Done,
                    contentDescription = "",
                )
            }
        }

        TutorialItem(
            modifier = Modifier.padding(start = 30.dp),
            step = steps[0],
            onPositioned = { tutorial, rect ->
                targetPositions[tutorial] = rect
            },
        ) { modifier ->
            FilledTonalButton(
                modifier = modifier,
                onClick = { currentStepIndex++ },
            ) {
                Text("Button")
            }
        }

        TutorialItem(
            modifier = Modifier.padding(start = 99.dp),
            step = steps[2],
            onPositioned = { tutorial, rect ->
                targetPositions[tutorial] = rect
            },
        ) { modifier ->
            Box(modifier = modifier.size(90.dp).background(Color.Magenta))
        }
    }

    AnimatedVisibility(visible = showTutorial && currentStepIndex < steps.size) {
        val step = steps[minOf(currentStepIndex, steps.size - 1)]
        val targetRect = targetPositions[step]

        targetRect?.let { rect ->
            TutorialOverlay(
                targetBounds = rect,
                step = step,
                onClose = { currentStepIndex++ },
            )
        }
    }
}

@Composable
fun TutorialItem(
    modifier: Modifier = Modifier,
    step: TutorialStep,
    onPositioned: (TutorialStep, Rect) -> Unit,
    content: @Composable (Modifier) -> Unit,
) {
    content(
        modifier.then(
            Modifier.onGloballyPositioned { layoutCoordinates ->
                val position = layoutCoordinates.positionInParent()
                val size = layoutCoordinates.size
                val rect =
                    Rect(
                        left = position.x,
                        top = position.y,
                        right = position.x + size.width,
                        bottom = position.y + size.height,
                    )
                onPositioned(step, rect)
            },
        ),
    )
}

@Composable
fun TutorialOverlay(
    targetBounds: Rect,
    step: TutorialStep,
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
                            addRoundRect(
                                RoundRect(
                                    rect = targetBounds,
                                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                                ),
                            )
                            // Use EvenOdd to create a hole
                            fillType = PathFillType.EvenOdd
                        }

                    // Draw the semi-transparent overlay with the hole
                    drawPath(
                        path = overlayPath,
                        color = Color.Black.copy(alpha = 0.55f),
                        style = Fill,
                    )

                    // Define the path for the stroke around the hole
                    val strokePath =
                        Path().apply {
                            addRoundRect(
                                RoundRect(
                                    rect = targetBounds,
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
                }.then(
                    if (step.isModal) {
                        Modifier.clickable(enabled = false) {
                            // Prevent clicks from passing through
                        }
                    } else {
                        Modifier
                    },
                ),
    ) {
        Box(
            modifier =
                Modifier
                    .offset {
                        IntOffset(
                            x = targetBounds.left.roundToInt(),
                            y = (targetBounds.bottom + 16).roundToInt(),
                        )
                    }
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(16.dp)
                    .widthIn(max = 200.dp),
        ) {
            Column {
                Text(text = step.description, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
                if (step.isModal) {
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        TextButton(onClick = onClose) {
                            Text("Got it")
                        }
                    }
                }
            }
        }
    }
}
