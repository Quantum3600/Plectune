package com.trishit.plectune.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import com.trishit.plectune.ui.utils.DampedDragAnimation
import kotlin.math.abs


@Composable
fun LiquidSlider(
    modifier: Modifier = Modifier,
    value: () -> Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    visibilityThreshold: Float,
    backdrop: Backdrop,
    colors: SliderColors = SliderDefaults.colors(),
    dragSensitivity: Float = 1f, // Added parameter for fine-tuning
    snapPoints: List<Float> = remember { listOf(30f, 75f, 135f, 195f, 240f) }, // Default snap points
    snapThreshold: Dp = 5.dp, // Threshold for snapping
    onInteractionStart: (() -> Unit)? = null
) {
    val trackBackdrop = rememberLayerBackdrop()
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val animationScope = rememberCoroutineScope()
    var didDrag by remember { mutableStateOf(false) }

    var internalValue by remember { mutableFloatStateOf(value()) }

    // Use a MutableState to hold the DampedDragAnimation instance
    val dampedDragAnimationMutableState = remember { mutableStateOf<DampedDragAnimation?>(null) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 20.dp), // Add padding for dots and labels
            contentAlignment = Alignment.CenterStart
        ) {
            val trackWidth = constraints.maxWidth
            val density = LocalDensity.current
            val textMeasurer = rememberTextMeasurer()
            val snapLabelStyle = TextStyle(fontSize = 10.sp)

            // Initialize DampedDragAnimation only once
            val currentInternalValue = internalValue // Capture initial internalValue
            LaunchedEffect(animationScope, trackWidth) {
                if (dampedDragAnimationMutableState.value == null) {
                    dampedDragAnimationMutableState.value = DampedDragAnimation(
                        animationScope = animationScope,
                        initialValue = currentInternalValue,
                        valueRange = valueRange,
                        visibilityThreshold = visibilityThreshold,
                        initialScale = 1f,
                        pressedScale = 1.5f,
                        onDragStarted = {
                            onInteractionStart?.invoke()
                        },
                        onDragStopped = {
                            // Snapping logic
                            val closestSnapPoint = snapPoints.minByOrNull { abs(it - internalValue) }
                            val finalValue = if (closestSnapPoint != null && abs(closestSnapPoint - internalValue) <= snapThreshold.value) {
                                closestSnapPoint
                            } else {
                                internalValue
                            }
                            internalValue = finalValue // Update internal state after potential snap
                            dampedDragAnimationMutableState.value?.animateToValue(finalValue) // Now safe to call
                            onValueChange(finalValue) // Inform parent of the final value
                            didDrag = false // Reset didDrag after drag stops
                        },
                        onDrag = { _, dragAmount ->
                            if (!didDrag) {
                                didDrag = dragAmount.x != 0f
                            }
                            val delta = (valueRange.endInclusive - valueRange.start) * (dragAmount.x / trackWidth) * dragSensitivity
                            val newValue = if (isLtr) (internalValue + delta).coerceIn(valueRange) else (internalValue - delta).coerceIn(valueRange)
                            internalValue = newValue // Update internal state during drag
                            dampedDragAnimationMutableState.value?.updateValue(newValue) // Now safe to call
                            onValueChange(newValue) // Update parent in real-time during drag
                        }
                    )
                }
            }

            val dampedDragAnimation = dampedDragAnimationMutableState.value // Get the actual instance

            // Only render slider components if dampedDragAnimation is initialized
            if (dampedDragAnimation != null) {

                // Effect to update internalValue and animation when external 'value' changes
                LaunchedEffect(value(), dampedDragAnimation) {
                    if (!didDrag && internalValue != value()) { // Only update if not dragging and external value changed
                        internalValue = value()
                        dampedDragAnimation.animateToValue(value())
                    }
                }

                // Draw snap points (dots and labels)
                snapPoints.forEach { snapValue ->
                    val progress = (snapValue - valueRange.start) / (valueRange.endInclusive - valueRange.start)
                    val xPositionPx = trackWidth * progress

                    // Dot
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    x = xPositionPx.toInt() - with(density) { (4.dp / 2).roundToPx() }, // Center the dot (dot size 4.dp)
                                    y = -with(density) { 15.dp.toPx() }.toInt() // Adjust vertical offset above track
                                )
                            }
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.7f))
                    )

                    // Label
                    val labelText = snapValue.toInt().toString()
                    val labelWidthPx = textMeasurer.measure(labelText, style = snapLabelStyle).size.width

                    Text(
                        text = labelText,
                        color = Color.White,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    x = xPositionPx.fastRoundToInt() - (labelWidthPx / 2),
                                    y = with(density) { 16.dp.toPx() }.toInt() // Adjust vertical offset below track
                                )
                            }
                    )
                }

                // Slider Track
                Box(Modifier.layerBackdrop(trackBackdrop)) {
                    Box(
                        Modifier
                            .clip(Capsule)
                            .background(colors.inactiveTrackColor)
                            .pointerInput(animationScope, dampedDragAnimation, trackWidth) {
                                detectTapGestures (
                                    onPress = {
                                        onInteractionStart?.invoke()
                                        tryAwaitRelease()
                                    },
                                    onTap = { position ->
                                        val delta = (valueRange.endInclusive - valueRange.start) * (position.x / trackWidth)
                                        val tappedValue =
                                            (if (isLtr) valueRange.start + delta
                                            else valueRange.endInclusive - delta)
                                                .coerceIn(valueRange)

                                        // Snapping logic for tap gestures
                                        val closestSnapPoint = snapPoints.minByOrNull { abs(it - tappedValue) }
                                        val finalValue = if (closestSnapPoint != null && abs(closestSnapPoint - tappedValue) <= snapThreshold.value) {
                                            closestSnapPoint
                                        } else {
                                            tappedValue
                                        }

                                        // Update internal value immediately
                                        internalValue = finalValue

                                        // Update parent immediately for real-time display
                                        onValueChange(finalValue)

                                        // Then animate to the value
                                        dampedDragAnimation.animateToValue(finalValue)
                                    }
                                )
                            }
                            .height(6f.dp)
                            .fillMaxWidth()
                    )

                    Box(
                        Modifier
                            .clip(Capsule)
                            .background(colors.activeTrackColor)
                            .height(6f.dp)
                            .layout { measurable, constraints ->
                                val placeable = measurable.measure(constraints)
                                val width = (trackWidth * dampedDragAnimation.progress).fastRoundToInt()
                                layout(width, placeable.height) {
                                    placeable.place(0, 0)
                                }
                            }
                    )
                }

                // Slider Thumb
                Box(
                    Modifier
                        .graphicsLayer {
                            translationX =
                                (-size.width / 2f + trackWidth * dampedDragAnimation.progress)
                                    .fastCoerceIn(-size.width / 4f, trackWidth - size.width * 3f / 4f) * if (isLtr) 1f else -1f
                        }
                        .then(dampedDragAnimation.modifier)
                        .drawBackdrop(
                            backdrop = rememberCombinedBackdrop(
                                backdrop,
                                rememberBackdrop(trackBackdrop) { drawBackdrop ->
                                    val progress = dampedDragAnimation.pressProgress
                                    val scaleX = lerp(2f / 3f, 1f, progress)
                                    val scaleY = lerp(0f, 1f, progress)
                                    scale(scaleX, scaleY) {
                                        drawBackdrop()
                                    }
                                }
                            ),
                            shape = { Capsule },
                            effects = {
                                val progress = dampedDragAnimation.pressProgress
                                blur(8f.dp.toPx() * (1f - progress))
                                lens(
                                    10f.dp.toPx() * progress,
                                    14f.dp.toPx() * progress,
                                    chromaticAberration = true
                                )
                            },
                            highlight = {
                                val progress = dampedDragAnimation.pressProgress
                                Highlight.Ambient.copy(
                                    width = Highlight.Ambient.width / 1.5f,
                                    blurRadius = Highlight.Ambient.blurRadius / 1.5f,
                                    alpha = progress
                                )
                            },
                            shadow = {
                                Shadow(
                                    radius = 4f.dp,
                                    color = Color.Black.copy(alpha = 0.05f)
                                )
                            },
                            innerShadow = {
                                val progress = dampedDragAnimation.pressProgress
                                InnerShadow(
                                    radius = 4f.dp * progress,
                                    alpha = progress
                                )
                            },
                            layerBlock = {
                                scaleX = dampedDragAnimation.scaleX
                                scaleY = dampedDragAnimation.scaleY
                                val velocity = dampedDragAnimation.velocity / 10f
                                scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                                scaleY *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                            },
                            onDrawSurface = {
                                val progress = dampedDragAnimation.pressProgress
                                drawRect(Color.White.copy(alpha = 1f - progress))
                            }
                        )
                        .size(40f.dp, 24f.dp)
                )
            }
        }
    }
}