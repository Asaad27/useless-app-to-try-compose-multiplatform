package com.asaad27.life.ui.component.helper

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.geometry.Offset
import com.asaad27.life.model.DonutItem
import com.asaad27.life.ui.component.DonutScale
import com.asaad27.life.ui.component.DonutSegment
import com.asaad27.life.ui.component.DonutSegmentInfo
import com.asaad27.life.util.Radians
import com.asaad27.life.util.toDegree
import kotlin.math.atan2

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
        if (segments.isEmpty()) return -1
        if (angle < 0f || angle > 360f) return -1

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
        return (Radians(
            atan2(
                (tapOffset.y - center.y).toDouble(),
                (tapOffset.x - center.x).toDouble()
            ).toFloat()
        ).toDegree().value + 360f) % 360f
    }
}