package com.asaad27.life

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.EaseInOutCirc
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeViewport
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.browser.document
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {

        val viewModel = DonutChartViewModel<String>()
        val uiState by viewModel.uiState.collectAsState()

        DonutChart(
            strokeWidth = 80.dp,
            data = listOf(
                DonutItem("item1", Color(0xFF7986CB), 1, "Item 1"),
                DonutItem("item2", Color(0xFF4FC3F7), 20, "Item 2"),
                DonutItem("item3", Color(0xFF4DB6AC), 30, "Item 3"),
                DonutItem("item4", Color(0xFFAED581), 40, "Item 4")
            ),
            spacingDegrees = 2f,
            animateProgressSpec = tween(2000, easing = EaseInOutCirc),
            animateAlphaSpec = tween(2000),
            uiState = uiState,
            onItemClick = viewModel::onItemClick
        )
    }
}


data class DonutItem<T>(
    val data: T,
    val color: Color,
    val weight: Int,
    val label: String
)

data class DonutChartUiState<T>(
    val clickedIndex: Int? = null,
    val shouldAnimate: Boolean = false,
    val isScaled: Boolean = false
)

class DonutChartViewModel<T> : ViewModel() {
    private val _uiState = MutableStateFlow(DonutChartUiState<T>())
    val uiState: StateFlow<DonutChartUiState<T>> = _uiState.asStateFlow()

    fun onItemClick(index: Int, item: DonutItem<T>) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                when {
                    currentState.clickedIndex == index -> currentState.copy(
                        clickedIndex = null,
                        isScaled = false
                    )

                    else -> currentState.copy(
                        clickedIndex = index,
                        isScaled = true
                    )
                }
            }
        }
    }
}

fun Float.toDegrees(radians: Double) = radians * 180 / 3.14

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

    val density = LocalDensity.current
    val strokeWidthPx = remember(strokeWidth) { with(density) { strokeWidth.toPx() } }

    val segmentScales = data.indices.map { index ->
        val targetScale = if (index == uiState.clickedIndex && uiState.isScaled) 1.05f else 1f
        val transition = updateTransition(targetScale, label = "ScaleTransition-$index")
        transition.animateFloat(
            label = "Scale-$index",
            transitionSpec = {
                tween(200, easing = FastOutSlowInEasing)
            }
        ) { scale -> scale }
    }

    val totalWeight = remember(data) { data.sumOf { it.weight } }
    val sweepAngles = remember(data, totalWeight, spacingDegrees) {
        val availableDegrees = 360f - (spacingDegrees * data.size)
        data.map { it.weight.toFloat() / totalWeight * availableDegrees }
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
                    val clickAngle = ((
                            kotlin.math.atan2(
                                (offset.y - center.y).toDouble(),
                                (offset.x - center.x).toDouble()
                            ) * 180 / 3.14
                            ).toFloat() + 360) % 360

                    var currentAngle = 0f
                    data.zip(sweepAngles).forEachIndexed { index, (item, sweep) ->
                        val segmentEnd = currentAngle + sweep
                        if (clickAngle in currentAngle..segmentEnd) {
                            onItemClick(index, item)
                            return@detectTapGestures
                        }
                        currentAngle += sweep + spacingDegrees
                    }
                }
            }
    ) {
        val canvasCenter = Offset(size.width / 2, size.height / 2)
        val radius = (size.width.coerceAtMost(size.height) - strokeWidthPx) / 2

        var startAngle = 0f
        data.zip(sweepAngles).forEachIndexed { index, (item, sweep) ->
            val scale = segmentScales[index].value
            val scaledRadius = radius * scale
            val topLeft = Offset(
                x = canvasCenter.x - scaledRadius,
                y = canvasCenter.y - scaledRadius
            )

            drawArc(
                color = item.color,
                alpha = alpha,
                startAngle = startAngle,
                sweepAngle = sweep * progress,
                useCenter = false,
                style = Stroke(strokeWidthPx),
                topLeft = topLeft,
                size = Size(scaledRadius * 2, scaledRadius * 2)
            )
            startAngle += sweep + spacingDegrees
        }
    }
}