package com.asaad27.life.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asaad27.life.dal.TransactionRepository
import com.asaad27.life.model.Transaction
import com.asaad27.life.state.LastTransactionsEvent
import com.asaad27.life.state.LastTransactionsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LastTransactionsViewModel(
    private val repository: TransactionRepository,
    private val pageSize: Int = 20
) : ViewModel() {
    private val _uiState = MutableStateFlow(LastTransactionsState())
    val uiState: StateFlow<LastTransactionsState> = _uiState.asStateFlow()
    private var currentPageKey: String? = null

    init {
        loadTransactions()
    }

    fun onEvent(event: LastTransactionsEvent) {
        viewModelScope.launch {
            when (event) {
                is LastTransactionsEvent.TransactionClicked -> handleTransactionClick(event.transaction)
                is LastTransactionsEvent.LoadTransactions -> loadTransactions()
                is LastTransactionsEvent.DeleteTransaction -> handleDeleteTransaction(event.transaction)
                is LastTransactionsEvent.LoadMoreTransactions -> loadMoreTransactions()
            }
        }
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val pagedData = repository.getTransactions(pageSize)
                currentPageKey = pagedData.nextKey
                _uiState.update {
                    it.copy(
                        transactions = pagedData.items,
                        isLoading = false,
                        hasMore = pagedData.hasMore,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun loadMoreTransactions() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMore) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            try {
                val pagedData = repository.getTransactions(pageSize, currentPageKey)
                currentPageKey = pagedData.nextKey
                _uiState.update { state ->
                    state.copy(
                        transactions = state.transactions + pagedData.items,
                        hasMore = pagedData.hasMore,
                        isLoadingMore = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Failed to load more transactions: ${e.message}",
                        isLoadingMore = false
                    )
                }
            }
        }
    }

    private fun handleTransactionClick(transaction: Transaction) {
        // TODO: Implement transaction click handling
        // This could navigate to transaction details or trigger other actions
    }

    private suspend fun handleDeleteTransaction(transaction: Transaction) {
        try {
            repository.deleteTransaction(transaction)
            loadTransactions() // Reload the list after deletion
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Failed to delete transaction: ${e.message}") }
        }
    }
}