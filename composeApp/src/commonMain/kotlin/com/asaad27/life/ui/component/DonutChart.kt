package com.asaad27.life.ui.component

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.asaad27.life.model.DonutItem
import com.asaad27.life.state.DonutChartEvent
import com.asaad27.life.ui.animation.rememberProgressAnimationState
import com.asaad27.life.ui.component.helper.DonutCalculator

data class DonutSegment<T>(
    val item: DonutItem<T>,
    val startAngle: Float,
    val endAngle: Float,
    val sweep: Float,
    val index: Int
)

data class DonutSegmentInfo<T>(
    val segments: List<DonutSegment<T>>,
    val availableDegrees: Float
)

data class DonutScale(
    val scale: Float,
    val animationSpec: AnimationSpec<Float>
)

data class DonutChartDimensionSpecs(
    val strokeWidth: Dp = 80.dp,
    val spacingDegree: Float = 2f
)


@Composable
fun <T> DonutChart(
    modifier: Modifier = Modifier,
    data: List<DonutItem<T>>,
    dimensions: DonutChartDimensionSpecs = DonutChartDimensionSpecs(),
    animateProgressSpec: AnimationSpec<Float>? = null,
    animateAlphaSpec: AnimationSpec<Float>? = null,
    clickedIndex: Int? = null,
    isScaled: Boolean = false,
    shouldAnimate: Boolean = true,
    contentDescription: String = "",
    onEvent: (DonutChartEvent) -> Unit = {}
) {
    val spacingDegree = dimensions.spacingDegree
    val strokeWidth = dimensions.strokeWidth

    require(data.isNotEmpty()) { "Data list cannot be empty" }
    require(spacingDegree * data.size < 360f) { "Total spacing cannot exceed 360 degrees" }
    require(strokeWidth > 0.dp) { "Stroke width must be positive" }

    val segmentInfo = remember(data, spacingDegree) {
        DonutCalculator.calculateSegments(data, spacingDegree)
    }
    val segmentScales = remember(segmentInfo.segments, clickedIndex, isScaled) {
        DonutCalculator.createScales(
            segments = segmentInfo.segments,
            clickedIndex = clickedIndex,
            isScaled = isScaled
        )
    }


    val drawingProgress =
        rememberProgressAnimationState(animateProgressSpec, shouldAnimate, "drawingProgress")
    val alphaProgress =
        rememberProgressAnimationState(animateAlphaSpec, shouldAnimate, "alphaProgress")

    LaunchedEffect(drawingProgress, alphaProgress) {
        if (drawingProgress >= 1f && alphaProgress >= 1f) {
            onEvent(DonutChartEvent.AnimationCompleted)
        }
    }

    val handleClick = rememberDonutClickHandler(segmentInfo.segments, onEvent)
    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .padding(strokeWidth / 2)
            .semantics {
                this.contentDescription = contentDescription
            }
            .pointerInput(segmentInfo.segments) {
                detectTapGestures { offset ->
                    val center = Offset(this.size.width / 2f, this.size.height / 2f)
                    handleClick(offset, center)
                }
            }
    ) {
        drawDonutSegments(
            segments = segmentInfo.segments,
            scales = segmentScales,
            progress = drawingProgress,
            alpha = alphaProgress,
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
    if (segments.isEmpty() || progress <= 0f || alpha <= 0f) return

    val canvasCenter = Offset(size.width / 2, size.height / 2)
    val radius = (size.width.coerceAtMost(size.height) - strokeWidth.toPx()) / 2
    val strokeWidthPx = strokeWidth.toPx()

    segments.forEachIndexed { index, segment ->
        val scale = scales[index].scale
        if (scale <= 0f) return@forEachIndexed
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
            style = Stroke(strokeWidthPx),
            topLeft = topLeft,
            size = Size(scaledRadius * 2, scaledRadius * 2)
        )
    }
}

@Composable
private fun <T> rememberDonutClickHandler(
    segments: List<DonutSegment<T>>,
    onEvent: (DonutChartEvent) -> Unit
): (Offset, Offset) -> Unit {
    return remember(segments, onEvent) {
        { offset, center ->
            val clickAngle = DonutCalculator.calculateClickAngle(offset, center)
            val clickedIndex = DonutCalculator.findClickedSegment(
                angle = clickAngle,
                segments = segments
            )

            if (clickedIndex >= 0) {
                onEvent(DonutChartEvent.SegmentClicked(clickedIndex, segments[clickedIndex].item))
            }
        }
    }
}