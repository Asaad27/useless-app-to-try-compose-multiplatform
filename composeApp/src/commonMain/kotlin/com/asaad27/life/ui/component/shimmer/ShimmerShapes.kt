package com.asaad27.life.ui.component.shimmer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp


@Composable
fun ShimmerBox(
    brush: Brush,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(brush)
    )
}

@Composable
fun ShimmerCircle(
    brush: Brush,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .background(brush, shape = CircleShape)
    )
}

@Composable
fun ShimmerLine(
    brush: Brush,
    height: Dp,
    widthFraction: Float = 1f,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .background(brush)
    )
}
