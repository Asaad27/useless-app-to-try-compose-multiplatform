package com.asaad27.life.dal

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Done
import com.asaad27.life.model.ExpenseCategory
import com.asaad27.life.model.PagedData
import com.asaad27.life.model.Transaction
import com.asaad27.life.util.IconResource
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days
import kotlin.random.Random

class FakeTransactionRepository : TransactionRepository {
    private val fakeCategories = listOf(
        ExpenseCategory(
            id = "groceries",
            name = "Groceries",
            icon = IconResource(Icons.Outlined.Add, "Groceries"),
            totalAmount = 450.0
        ),
        ExpenseCategory(
            id = "entertainment",
            name = "Entertainment",
            icon = IconResource(Icons.Outlined.Call, "Entertainment"),
            totalAmount = 200.0
        ),
        ExpenseCategory(
            id = "transport",
            name = "Transport",
            icon = IconResource(Icons.Outlined.Done, "Transport"),
            totalAmount = 150.0
        ),
        ExpenseCategory(
            id = "income",
            name = "Income",
            icon = IconResource(Icons.Outlined.Add, "Income"),
            totalAmount = 3000.0
        )
    )

    private val allTransactions = buildList {

        val currentTime = Clock.System.now()

        // Add salary
        add(
            Transaction(
                id = "income_1",
                timestampMs = currentTime.toEpochMilliseconds(),
                amount = 3000.0,
                description = "Monthly Salary",
                expenseCategory = fakeCategories.find { it.id == "income" }
            )
        )

        // Add regular expenses over the last 30 days
        for (daysAgo in 0..30) {
            val timestamp = currentTime.minus(daysAgo.days)

            // Groceries every 3-4 days
            if (daysAgo % 4 == 0) {
                add(
                    Transaction(
                        id = "groceries_$daysAgo",
                        timestampMs = timestamp.toEpochMilliseconds(),
                        amount = -45.0 - (Random.nextInt(10) * 30),
                        description = "Grocery Shopping",
                        expenseCategory = fakeCategories.find { it.id == "groceries" }
                    )
                )
            }

            // Entertainment expenses twice a week
            if (daysAgo % 7 == 0 || daysAgo % 7 == 3) {
                add(
                    Transaction(
                        id = "entertainment_$daysAgo",
                        timestampMs = timestamp.toEpochMilliseconds(),
                        amount = -25.0 - (Random.nextInt(10) * 20),
                        description = if (Random.nextInt(10) > 0.5) "Movie Night" else "Restaurant",
                        expenseCategory = fakeCategories.find { it.id == "entertainment" }
                    )
                )
            }

            // Transport expenses every other day
            if (daysAgo % 2 == 0) {
                add(
                    Transaction(
                        id = "transport_$daysAgo",
                        timestampMs = timestamp.toEpochMilliseconds(),
                        amount = -10.0 - (Random.nextInt(10) * 5),
                        description = "Public Transport",
                        expenseCategory = fakeCategories.find { it.id == "transport" }
                    )
                )
            }
        }
    }.sortedByDescending { it.timestampMs }

    override suspend fun getTransactions(pageSize: Int, pageKey: String?): PagedData<Transaction> {
        // Simulate network delay
        delay(500)

        val startIndex = if (pageKey != null) {
            pageKey.toIntOrNull() ?: 0
        } else 0

        val endIndex = (startIndex + pageSize).coerceAtMost(allTransactions.size)
        val hasMore = endIndex < allTransactions.size

        return PagedData(
            items = allTransactions.subList(startIndex, endIndex),
            hasMore = hasMore,
            nextKey = if (hasMore) endIndex.toString() else null
        )
    }

    override suspend fun addTransaction(transaction: Transaction) {
        throw UnsupportedOperationException("Fake repository doesn't support adding transactions")
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        throw UnsupportedOperationException("Fake repository doesn't support deleting transactions")
    }
}