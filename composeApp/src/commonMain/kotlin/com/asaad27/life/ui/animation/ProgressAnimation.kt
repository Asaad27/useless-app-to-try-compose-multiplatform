package com.asaad27.life.ui.animation

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun rememberProgressAnimationState(
    spec: AnimationSpec<Float>?,
    shouldAnimate: Boolean,
    label: String
): Float {
    var isAnimating by remember { mutableStateOf(false) }

    return spec?.let {
        val progress by animateFloatAsState(
            targetValue = if (shouldAnimate) 1f else 0f,
            animationSpec = spec,
            label = label,
            finishedListener = { isAnimating = false }
        )
        LaunchedEffect(shouldAnimate) {
            if (shouldAnimate) {
                isAnimating = true
            }
        }
        progress
    } ?: 1f
}