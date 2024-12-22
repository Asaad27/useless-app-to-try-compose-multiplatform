package com.asaad27.life.model

import com.asaad27.life.util.IconResource


data class ExpenseCategory(
    val id: String,
    val name: String,
    val icon: IconResource,
    val totalAmount: Double
)

