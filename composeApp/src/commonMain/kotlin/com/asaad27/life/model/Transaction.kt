package com.asaad27.life.model

data class Transaction(
    val id: String,
    val timestampMs: Long,
    val amount: Double,
    val description: String,
    val expenseCategory: ExpenseCategory? = null
)