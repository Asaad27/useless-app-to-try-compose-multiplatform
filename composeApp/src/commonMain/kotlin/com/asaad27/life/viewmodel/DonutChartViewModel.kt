package com.asaad27.life.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asaad27.life.model.DonutItem
import com.asaad27.life.model.SpendingCategory
import com.asaad27.life.repository.SpendingCategoryRepository
import com.asaad27.life.state.DonutChartEvent
import com.asaad27.life.state.DonutChartState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DonutChartViewModel(
    private val repository: SpendingCategoryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DonutChartState())
    val uiState: StateFlow<DonutChartState> = _uiState.asStateFlow()

    private val _spendingCategories = MutableStateFlow<List<SpendingCategory>>(emptyList())
    val spendingCategories: StateFlow<List<SpendingCategory>> = _spendingCategories.asStateFlow()


    init {
        loadCategories()
        startInitialAnimation()
    }

    fun onEvent(event: DonutChartEvent) {
        viewModelScope.launch {
            when (event) {
                is DonutChartEvent.SegmentClicked<*> -> {
                    handleSegmentClick(event)
                }
                is DonutChartEvent.AnimationCompleted -> handleAnimationCompleted()
            }
        }
    }

    fun getDonutItems(): List<DonutItem<String>> {

        val totalAmount = spendingCategories.value.sumOf { it.totalAmount }

        return spendingCategories.value.map { category ->
            DonutItem(
                data = category.id,
                color = category.color,
                percentage = category.totalAmount.toFloat() / totalAmount.toFloat() ,
                label = category.name
            )
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                _spendingCategories.value = repository.getSpendingCategories()
            } catch (e: Exception) {
                //todo handle error
            }
        }
    }

    private fun startInitialAnimation() {
        viewModelScope.launch {
            _uiState.update { it.copy(animationState = DonutChartState.AnimationState.InProgress) }
        }
    }

    private fun resetState() {
        viewModelScope.launch {
            _uiState.update {
                DonutChartState(
                    animationState = DonutChartState.AnimationState.NotStarted
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        resetState()
    }

    private fun handleSegmentClick(event: DonutChartEvent.SegmentClicked<*>) {
        _uiState.update { currentState ->
            when {
                currentState.clickedIndex == event.index -> currentState.copy(
                    clickedIndex = null,
                    isScaled = false
                )
                else -> currentState.copy(
                    clickedIndex = event.index,
                    isScaled = true
                )
            }
        }
    }

    private fun handleAnimationCompleted() {
        _uiState.update { it.copy(animationState = DonutChartState.AnimationState.Completed) }
    }
}