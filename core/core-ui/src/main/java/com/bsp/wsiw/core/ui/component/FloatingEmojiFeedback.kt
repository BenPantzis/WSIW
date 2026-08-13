package com.bsp.wsiw.core.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FloatingEmojiFeedback(
    emoji: String,
    origin: Offset,
    accent: Color,
    onFinished: () -> Unit,
) {
    val density = LocalDensity.current

    val scale = remember { Animatable(0f) }
    val travelY = remember { Animatable(0f) }
    val emojiAlpha = remember { Animatable(1f) }
    val particleRadius = remember { Animatable(0f) }
    val particleAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        val travelPx = with(density) { 96.dp.toPx() }
        val particlePx = with(density) { 38.dp.toPx() }
        launch { scale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 350f)) }
        launch { travelY.animateTo(-travelPx, tween(860, easing = FastOutSlowInEasing)) }
        launch {
            delay(300)
            emojiAlpha.animateTo(0f, tween(560))
            onFinished()
        }
        launch { particleRadius.animateTo(particlePx, tween(420, easing = FastOutSlowInEasing)) }
        launch {
            delay(70)
            particleAlpha.animateTo(0f, tween(380))
        }
    }

    val dotRadiusPx = with(density) { 4.dp.toPx() }
    val emojiHalfPx = with(density) { 20.dp.toPx() }

    Canvas(Modifier.fillMaxSize()) {
        val pa = particleAlpha.value
        val pr = particleRadius.value
        if (pa > 0f) {
            val color = accent.copy(alpha = pa * 0.8f)
            repeat(8) { i ->
                val angle = Math.toRadians(i * 45.0)
                drawCircle(
                    color = color,
                    radius = dotRadiusPx,
                    center = Offset(
                        origin.x + (pr * cos(angle)).toFloat(),
                        origin.y + (pr * sin(angle)).toFloat(),
                    ),
                )
            }
        }
    }

    Text(
        text = emoji,
        style = MaterialTheme.typography.displaySmall,
        modifier = Modifier
            .absoluteOffset {
                IntOffset(
                    x = (origin.x - emojiHalfPx).roundToInt(),
                    y = (origin.y - emojiHalfPx).roundToInt(),
                )
            }
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                translationY = travelY.value
                alpha = emojiAlpha.value
            },
    )
}
