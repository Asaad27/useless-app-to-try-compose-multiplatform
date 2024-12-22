package com.asaad27.life

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.asaad27.life.model.DonutItem
import com.asaad27.life.ui.component.DonutChart
import com.asaad27.life.viewmodel.DonutChartViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: DonutChartViewModel<String> by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DonutChartScreen(viewModel)
        }
    }
}

@Composable
fun <T> DonutChartScreen(
    viewModel: DonutChartViewModel<T>
) {
    val uiState by viewModel.uiState.collectAsState()

    DonutChart(
        data = listOf(
            DonutItem("item1", Color(0xFF7986CB), 1, "Item 1"),
            DonutItem("item2", Color(0xFF4FC3F7), 20, "Item 2"),
            DonutItem("item3", Color(0xFF4DB6AC), 30, "Item 3"),
            DonutItem("item4", Color(0xFFAED581), 40, "Item 4")
        ),
        clickedIndex = uiState.clickedIndex,
        isScaled = uiState.isScaled,
        shouldAnimate = uiState.shouldAnimate,
        onEvent = viewModel::onEvent
    )
}