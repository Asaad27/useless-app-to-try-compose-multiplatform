package com.asaad27.life.repository

import com.asaad27.life.model.PagedData
import com.asaad27.life.model.Transaction
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlin.random.Random
import kotlin.time.Duration.Companion.days

class FakeTransactionRepository : TransactionRepository {
    private val fakeCategories = FakeSpendingCategories.categories

    private val allTransactions = buildList {
        val currentTime = Clock.System.now()
        // Add salary
        add(
            Transaction(
                id = "income_1",
                timestampMs = Clock.System.now(),
                amount = 3000.0,
                description = "Monthly Salary",
                spendingCategory = fakeCategories.find { it.id == "income" }
            )
        )

        // Add regular expenses over the last 30 days
        for (daysAgo in 0..300) {
            val timestamp = currentTime.minus(daysAgo.days)

            // Groceries every 3-4 days
            if (daysAgo % 4 == 0) {
                add(
                    Transaction(
                        id = "groceries_$daysAgo",
                        timestampMs = timestamp,
                        amount = -45.0 - (Random.nextInt(10) * 30),
                        description = "Grocery Shopping",
                        spendingCategory = fakeCategories.find { it.id == "groceries" }
                    )
                )
            }

            // Entertainment expenses twice a week
            if (daysAgo % 7 == 0 || daysAgo % 7 == 3) {
                add(
                    Transaction(
                        id = "entertainment_$daysAgo",
                        timestampMs = timestamp,
                        amount = -25.0 - (Random.nextInt(10) * 20),
                        description = if (Random.nextInt(10) > 0.5) "Movie Night" else "Restaurant",
                        spendingCategory = fakeCategories.find { it.id == "entertainment" }
                    )
                )
            }

            // Transport expenses every other day
            if (daysAgo % 2 == 0) {
                add(
                    Transaction(
                        id = "transport_$daysAgo",
                        timestampMs = timestamp,
                        amount = -10.0 - (Random.nextInt(10) * 5),
                        description = "Public Transport",
                        spendingCategory = fakeCategories.find { it.id == "transport" }
                    )
                )
            }
        }
    }.sortedByDescending { it.timestampMs }

    override suspend fun getTransactions(pageSize: Int, pageKey: String?): PagedData<Transaction> {
        // Simulate network delay
        delay(5000)

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