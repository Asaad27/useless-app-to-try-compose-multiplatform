package com.asaad27.life.state

data class DonutChartUiState<T>(
    val clickedIndex: Int? = null,
    val shouldAnimate: Boolean = false,
    val isScaled: Boolean = false
)