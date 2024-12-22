package com.asaad27.life.ui.component

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.asaad27.life.model.DonutItem
import com.asaad27.life.state.DonutChartUiState
import com.asaad27.life.util.radianToDegree

data class DonutSegment<T>(
    val item: DonutItem<T>,
    val startAngle: Float,
    val endAngle: Float,
    val sweep: Float,
    val index: Int
)

data class DonutSegmentInfo<T>(
    val segments: List<DonutSegment<T>>,
    val totalWeight: Int,
    val availableDegrees: Float
)

data class DonutScale(
    val scale: Float,
    val animationSpec: AnimationSpec<Float>
)

object DonutCalculator {
    fun <T> calculateSegments(
        data: List<DonutItem<T>>,
        spacingDegrees: Float
    ): DonutSegmentInfo<T> {
        require(data.isNotEmpty()) { "Data list cannot be empty" }
        require(spacingDegrees * data.size < 360f) { "Total spacing cannot exceed 360 degrees" }

        val totalWeight = data.sumOf { it.weight }
        val availableDegrees = 360f - (spacingDegrees * data.size)

        var currentAngle = 0f
        val segments = data.mapIndexed { index, item ->
            val sweep = (item.weight.toFloat() / totalWeight) * availableDegrees
            DonutSegment(
                item = item,
                startAngle = currentAngle,
                endAngle = currentAngle + sweep,
                sweep = sweep,
                index = index
            ).also {
                currentAngle += sweep + spacingDegrees
            }
        }

        return DonutSegmentInfo(
            segments = segments,
            totalWeight = totalWeight,
            availableDegrees = availableDegrees
        )
    }

    fun findClickedSegment(angle: Float, segments: List<DonutSegment<*>>): Int {
        return segments.binarySearch { segment ->
            when {
                angle < segment.startAngle -> 1
                angle > segment.endAngle -> -1
                else -> 0
            }
        }
    }

    fun <T> createScales(
        segments: List<DonutSegment<T>>,
        clickedIndex: Int?,
        isScaled: Boolean,
        scaleFactor: Float = 1.05f
    ): List<DonutScale> {
        return segments.map { segment ->
            val targetScale = if (segment.index == clickedIndex && isScaled) scaleFactor else 1f
            DonutScale(
                scale = targetScale,
                animationSpec = tween(200, easing = FastOutSlowInEasing)
            )
        }
    }

    fun calculateClickAngle(tapOffset: Offset, center: Offset): Float {
        return (kotlin.math.atan2(
            (tapOffset.y - center.y).toDouble(),
            (tapOffset.x - center.x).toDouble()
        ).toFloat().radianToDegree() + 360f) % 360f
    }
}

@Composable
fun <T> DonutChart(
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 20.dp,
    data: List<DonutItem<T>>,
    spacingDegrees: Float = 0f,
    animateProgressSpec: AnimationSpec<Float>? = null,
    animateAlphaSpec: AnimationSpec<Float>? = null,
    uiState: DonutChartUiState<T>,
    onItemClick: (Int, DonutItem<T>) -> Unit = { _, _ -> }
) {
    require(data.isNotEmpty()) { "Data list cannot be empty" }
    require(spacingDegrees * data.size < 360f) { "Total spacing cannot exceed 360 degrees" }
    require(strokeWidth > 0.dp) { "Stroke width must be positive" }

    val segmentInfo = remember(data, spacingDegrees) {
        DonutCalculator.calculateSegments(data, spacingDegrees)
    }
    val segmentScales = remember(segmentInfo.segments, uiState.clickedIndex, uiState.isScaled) {
        DonutCalculator.createScales(
            segments = segmentInfo.segments,
            clickedIndex = uiState.clickedIndex,
            isScaled = uiState.isScaled
        )
    }


    var shouldAnimate by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shouldAnimate = true }

    val progress by animateProgressSpec?.let { spec ->
        animateFloatAsState(
            targetValue = if (shouldAnimate) 1f else 0f,
            animationSpec = spec,
            label = "Progress"
        )
    } ?: remember { mutableFloatStateOf(1f) }
    val alpha by animateAlphaSpec?.let { spec ->
        animateFloatAsState(
            targetValue = if (shouldAnimate) 1f else 0f,
            animationSpec = spec,
            label = "Alpha"
        )
    } ?: remember { mutableFloatStateOf(1f) }

    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .padding(strokeWidth / 2)
            .pointerInput(data) {
                detectTapGestures { offset ->
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val clickAngle = DonutCalculator.calculateClickAngle(offset, center)
                    val clickedIndex = DonutCalculator.findClickedSegment(
                        angle = clickAngle,
                        segments = segmentInfo.segments
                    )

                    if (clickedIndex >= 0) {
                        onItemClick(clickedIndex, data[clickedIndex])
                    }
                }
            }
    ) {
        drawDonutSegments(
            segments = segmentInfo.segments,
            scales = segmentScales,
            progress = progress,
            alpha = alpha,
            strokeWidth = strokeWidth
        )
    }
}

private fun DrawScope.drawDonutSegments(
    segments: List<DonutSegment<*>>,
    scales: List<DonutScale>,
    progress: Float,
    alpha: Float,
    strokeWidth: Dp
) {
    val canvasCenter = Offset(size.width / 2, size.height / 2)
    val radius = (size.width.coerceAtMost(size.height) - strokeWidth.toPx()) / 2

    segments.forEachIndexed { index, segment ->
        val scale = scales[index].scale
        val scaledRadius = radius * scale

        val topLeft = Offset(
            x = canvasCenter.x - scaledRadius,
            y = canvasCenter.y - scaledRadius
        )

        drawArc(
            color = segment.item.color,
            alpha = alpha,
            startAngle = segment.startAngle,
            sweepAngle = segment.sweep * progress,
            useCenter = false,
            style = Stroke(strokeWidth.toPx()),
            topLeft = topLeft,
            size = Size(scaledRadius * 2, scaledRadius * 2)
        )
    }
}

@Composable
private fun rememberDonutAnimationState(
    spec: AnimationSpec<Float>?,
    shouldAnimate: Boolean
): Float {
    return spec?.let {
        val progress by animateFloatAsState(
            targetValue = if (shouldAnimate) 1f else 0f,
            animationSpec = spec,
            label = "Progress"
        )
        progress
    } ?: 1f
}