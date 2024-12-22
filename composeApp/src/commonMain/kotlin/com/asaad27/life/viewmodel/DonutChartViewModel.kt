package com.asaad27.life.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asaad27.life.state.DonutChartEvent
import com.asaad27.life.state.DonutChartState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DonutChartViewModel<T> : ViewModel() {
    private val _uiState = MutableStateFlow(DonutChartState())
    val uiState: StateFlow<DonutChartState> = _uiState.asStateFlow()

    init {
        startInitialAnimation()
    }

    private fun startInitialAnimation() {
        viewModelScope.launch {
            _uiState.update { it.copy(animationState = DonutChartState.AnimationState.InProgress) }
        }
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

    @Suppress("UNCHECKED_CAST")
    private fun handleSegmentClick(event: DonutChartEvent.SegmentClicked<*>) {
        val typedEvent = event as? DonutChartEvent.SegmentClicked<T> ?: return
        _uiState.update { currentState ->
            when {
                currentState.clickedIndex == typedEvent.index -> currentState.copy(
                    clickedIndex = null,
                    isScaled = false
                )
                else -> currentState.copy(
                    clickedIndex = typedEvent.index,
                    isScaled = true
                )
            }
        }
    }

    private fun handleAnimationCompleted() {
        _uiState.update { it.copy(animationState = DonutChartState.AnimationState.Completed) }
    }
}