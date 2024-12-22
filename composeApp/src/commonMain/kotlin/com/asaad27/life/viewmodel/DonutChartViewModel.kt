package com.asaad27.life.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asaad27.life.model.DonutItem
import com.asaad27.life.state.DonutChartUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DonutChartViewModel<T> : ViewModel() {
    private val _uiState = MutableStateFlow(DonutChartUiState<T>())
    val uiState: StateFlow<DonutChartUiState<T>> = _uiState.asStateFlow()

    fun onItemClick(index: Int, item: DonutItem<T>) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                when {
                    currentState.clickedIndex == index -> currentState.copy(
                        clickedIndex = null,
                        isScaled = false
                    )

                    else -> currentState.copy(
                        clickedIndex = index,
                        isScaled = true
                    )
                }
            }
        }
    }
}