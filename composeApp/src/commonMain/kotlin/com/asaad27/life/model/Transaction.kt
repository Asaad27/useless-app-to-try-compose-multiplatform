package com.asaad27.life.model

import kotlinx.datetime.Instant

data class Transaction(
    val id: String,
    val timestampMs: Instant,
    val amount: Double,
    val description: String,
    val spendingCategory: SpendingCategory? = null
)