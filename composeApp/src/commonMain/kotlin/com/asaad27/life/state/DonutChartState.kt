package com.asaad27.life.state

import com.asaad27.life.model.DonutItem

sealed interface DonutChartEvent {
    data class SegmentClicked<T>(val index: Int, val item: DonutItem<T>) : DonutChartEvent
    data object AnimationCompleted : DonutChartEvent
}

data class DonutChartState(
    val clickedIndex: Int? = null,
    val isScaled: Boolean = false,
    val animationState: AnimationState = AnimationState.NotStarted
) {
    sealed class AnimationState {
        data object NotStarted : AnimationState()
        data object InProgress : AnimationState()
        data object Completed : AnimationState()
    }

    val shouldAnimate: Boolean
        get() = animationState == AnimationState.InProgress ||
                animationState == AnimationState.NotStarted
}