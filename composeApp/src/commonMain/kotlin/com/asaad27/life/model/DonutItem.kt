package com.asaad27.life.model

import androidx.compose.ui.graphics.Color

data class DonutItem<T>(
    val data: T,
    val color: Color,
    val percentage: Float,
    val label: String
)