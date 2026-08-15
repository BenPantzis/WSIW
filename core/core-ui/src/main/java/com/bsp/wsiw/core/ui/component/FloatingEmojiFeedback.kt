package com.bsp.wsiw.core.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.drawText
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Canvas clips its drawing, so this needs to be large enough for the particle ring (38dp radius)
// and the emoji's upward travel (96dp). 240dp gives comfortable headroom for both.
private val AnimationCanvasSize = 240.dp

private fun Modifier.zeroSizeCenteredOverlay(size: Dp): Modifier = this.layout { measurable, _ ->
    val sizePx = size.roundToPx()
    val placeable = measurable.measure(Constraints.fixed(sizePx, sizePx))
    layout(0, 0) { placeable.place(-sizePx / 2, -sizePx / 2) }
}

@Composable
fun FloatingEmojiFeedback(
    emoji: String,
    accent: Color,
    onFinished: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val displaySmall = MaterialTheme.typography.displaySmall
    val textLayout = remember(emoji) { textMeasurer.measure(emoji, displaySmall) }

    val scale = remember { Animatable(0f) }
    val travelY = remember { Animatable(0f) }
    val emojiAlpha = remember { Animatable(1f) }
    val particleRadius = remember { Animatable(0f) }
    val particleAlpha = remember { Animatable(1f) }

    LaunchedEffect(emoji) {
        scale.snapTo(0f)
        travelY.snapTo(0f)
        emojiAlpha.snapTo(1f)
        particleRadius.snapTo(0f)
        particleAlpha.snapTo(1f)
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

    Canvas(modifier = modifier.zeroSizeCenteredOverlay(AnimationCanvasSize)) {
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
                        center.x + (pr * cos(angle)).toFloat(),
                        center.y + (pr * sin(angle)).toFloat(),
                    ),
                )
            }
        }

        val ea = emojiAlpha.value
        if (ea > 0f) {
            val sc = scale.value
            val ty = travelY.value
            val tw = textLayout.size.width.toFloat()
            val th = textLayout.size.height.toFloat()
            withTransform({
                translate(left = center.x - tw / 2f, top = center.y - th / 2f + ty)
                scale(scaleX = sc, scaleY = sc, pivot = Offset(tw / 2f, th / 2f))
            }) {
                drawText(textLayout, alpha = ea)
            }
        }
    }
}
