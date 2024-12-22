package com.asaad27.life.repository

import com.asaad27.life.model.SpendingCategory

interface SpendingCategoryRepository {
    suspend fun getSpendingCategories(): List<SpendingCategory>
    suspend fun updateSpendingCategory(category: SpendingCategory)
}



