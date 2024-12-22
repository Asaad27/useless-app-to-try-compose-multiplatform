package com.asaad27.life.model

import androidx.compose.ui.graphics.Color
import com.asaad27.life.util.IconResource


data class SpendingCategory(
    val id: String,
    val name: String,
    val icon: IconResource,
    val totalAmount: Double,
    val color: Color
)

