package com.asaad27.life.repository

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Done
import androidx.compose.ui.graphics.Color
import com.asaad27.life.model.SpendingCategory
import com.asaad27.life.util.IconResource

class FakeSpendingCategoryRepository : SpendingCategoryRepository {
    override suspend fun getSpendingCategories(): List<SpendingCategory> {
        return FakeSpendingCategories.categories
    }

    override suspend fun updateSpendingCategory(category: SpendingCategory) {
        // In a real implementation, this would update the data source
    }
}

object FakeSpendingCategories {
    val categories = listOf(
        SpendingCategory(
            id = "groceries",
            name = "Groceries",
            icon = IconResource(Icons.Outlined.Add, "Groceries"),
            totalAmount = 450.0,
            color = Color(0xFF7986CB)
        ),
        SpendingCategory(
            id = "entertainment",
            name = "Entertainment",
            icon = IconResource(Icons.Outlined.Call, "Entertainment"),
            totalAmount = 200.0,
            color = Color(0xFF4FC3F7),
        ),
        SpendingCategory(
            id = "transport",
            name = "Transport",
            icon = IconResource(Icons.Outlined.Done, "Transport"),
            totalAmount = 150.0,
            color = Color(0xFF4DB6AC)
        ),
        SpendingCategory(
            id = "income",
            name = "Income",
            icon = IconResource(Icons.Outlined.Add, "Income"),
            totalAmount = 3000.0,
            color = Color(0xFFAED581)
        )
    )
}