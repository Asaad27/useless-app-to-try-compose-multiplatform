package com.asaad27.life.dal

import com.asaad27.life.model.PagedData
import com.asaad27.life.model.Transaction

interface TransactionRepository {
    suspend fun getTransactions(pageSize: Int, pageKey: String? = null): PagedData<Transaction>
    suspend fun addTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
}


