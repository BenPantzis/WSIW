package com.bsp.wsiw.core.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.compositeOver

fun Modifier.shimmerEffect(): Modifier = composed {
    val shimmerBase = MaterialTheme.colorScheme.surfaceVariant
    val shimmerHighlight = MaterialTheme.colorScheme.onSurface
        .copy(alpha = 0.12f)
        .compositeOver(shimmerBase)
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateX by transition.animateFloat(
        initialValue = -600f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_x",
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(shimmerBase, shimmerHighlight, shimmerBase),
            start = Offset(translateX, 0f),
            end = Offset(translateX + 600f, 600f),
        ),
    )
}
