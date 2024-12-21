package com.asaad27.life

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.EaseInOutCirc
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.asaad27.life.utils.AndroidDevicesPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppAndroidPreview()
        }
    }
}

@AndroidDevicesPreview
@Composable
fun AppAndroidPreview() {
    DonutChart(
        strokeWidth = 80.dp,
        data = listOf(
            DonutItem(data = "item1", color = Color(0xFF7986CB), weight = 1, label = "Item 1"),
            DonutItem(data = "item2", color = Color(0xFF4FC3F7), weight = 20, label = "Item 2"),
            DonutItem(data = "item3", color = Color(0xFF4DB6AC), weight = 30, label = "Item 3"),
            DonutItem(data = "item4", color = Color(0xFFAED581), weight = 40, label = "Item 4")
        ),
        spacingDegrees = 2f,
        animateProgressSpec = tween(
            durationMillis = 2000,
            easing = EaseInOutCirc
        ),
        animateAlphaSpec = tween(durationMillis = 2000),
        onItemClick = { item ->
            println("Clicked on ${item.label}")
        }
    )
}

data class DonutItem<T>(
    val data: T,
    val color: Color,
    val weight: Int,
    val label: String
)

class DonutChartViewModel<T> : ViewModel() {
    private val _uiState = MutableStateFlow(DonutChartUiState<T>())
    val uiState = _uiState.asStateFlow()

    fun onItemClicked(index: Int, item: DonutItem<T>) {
        _uiState.update { it.copy(clickedIndex = index) }
    }

    fun startAnimation() {
        _uiState.update { it.copy(shouldAnimate = true) }
    }
}

data class DonutChartUiState<T>(
    val clickedIndex: Int? = null,
    val shouldAnimate: Boolean = false
)


@Composable
fun <T> DonutChart(
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 20.dp,
    data: List<DonutItem<T>>,
    spacingDegrees: Float = 0f,
    animateProgressSpec: AnimationSpec<Float>? = null,
    animateAlphaSpec: AnimationSpec<Float>? = null,
    onItemClick: (DonutItem<T>) -> Unit = {}
) {
    require(data.isNotEmpty()) { "Data list cannot be empty" }
    require(spacingDegrees * data.size < 360f) { "Total spacing cannot exceed 360 degrees" }
    require(strokeWidth > 0.dp) { "Stroke width must be positive" }

    var clickedIndex by remember { mutableStateOf<Int?>(null) }
    var clickPosition by remember { mutableStateOf<Offset?>(null) }

    val segmentScales = remember(data.size) {
        List(data.size) { _ ->
            Animatable(1f)
        }
    }

    LaunchedEffect(clickedIndex, clickPosition) {
        segmentScales.forEachIndexed { index, animatable ->
            if (index == clickedIndex) {
                animatable.animateTo(
                    targetValue = 1.1f,
                    animationSpec = tween(200, easing = FastOutSlowInEasing)
                )
            } else {
                animatable.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(200, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    val totalWeight = remember(data) {
        data.sumOf { it.weight }
    }

    val sweepAngles = remember(data, totalWeight) {
        val totalSpacingDegrees = spacingDegrees * data.size
        val availableDegrees = 360f - totalSpacingDegrees
        data.map {
            it.weight.toFloat() / totalWeight * availableDegrees
        }
    }

    var shouldAnimate by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        shouldAnimate = true
    }

    val progress by animateProgressSpec?.let { spec ->
        animateFloatAsState(
            targetValue = if (shouldAnimate) 1f else 0f,
            animationSpec = spec,
            label = "DonutProgressAnimation"
        )
    } ?: remember { mutableFloatStateOf(1f) }

    val alpha by animateAlphaSpec?.let { spec ->
        animateFloatAsState(
            targetValue = if (shouldAnimate) 1f else 0f,
            animationSpec = spec,
            label = "DonutAlphaAnimation"
        )
    } ?: remember { mutableFloatStateOf(1f) }


    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .padding(8.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    clickPosition = offset
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val clickAngle = (Math.toDegrees(
                        kotlin.math.atan2(
                            (offset.y - center.y).toDouble(),
                            (offset.x - center.x).toDouble()
                        )
                    ).toFloat() + 360) % 360

                    var currentAngle = 0f
                    data.zip(sweepAngles).forEachIndexed { index, (item, sweep) ->
                        val segmentEnd = currentAngle + sweep
                        if (clickAngle in currentAngle..segmentEnd) {
                            clickedIndex = index
                            onItemClick(item)
                            return@detectTapGestures
                        }
                        currentAngle += sweep + spacingDegrees
                    }
                }
            }
    ) {
        val outerRectWidth = size.width
        val strokeWidthPx = strokeWidth.toPx()
        val diameter = outerRectWidth - strokeWidthPx
        val offset = strokeWidthPx / 2

        var currentStartAngle = 0f
        data.zip(sweepAngles).forEachIndexed  { index, (item, sweep) ->
            val scale = segmentScales[index].value
            drawArc(
                color = item.color,
                alpha = alpha,
                style = Stroke(strokeWidthPx),
                startAngle = currentStartAngle,
                sweepAngle = sweep * progress,
                useCenter = false,
                topLeft = Offset(
                    x = offset + (1 - scale) * diameter / 2,
                    y = offset + (1 - scale) * diameter / 2
                ),
                size = Size(diameter * scale, diameter * scale)
            )

            currentStartAngle += sweep + spacingDegrees
        }
    }
}