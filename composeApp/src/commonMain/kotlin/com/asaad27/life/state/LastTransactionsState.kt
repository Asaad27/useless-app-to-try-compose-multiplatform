package com.asaad27.life.state

import com.asaad27.life.model.Transaction


sealed interface LastTransactionsEvent {
    data class TransactionClicked(val transaction: Transaction) : LastTransactionsEvent
    data object LoadTransactions : LastTransactionsEvent
    data object LoadMoreTransactions : LastTransactionsEvent
    data class DeleteTransaction(val transaction: Transaction) : LastTransactionsEvent
}

data class LastTransactionsState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = true
)