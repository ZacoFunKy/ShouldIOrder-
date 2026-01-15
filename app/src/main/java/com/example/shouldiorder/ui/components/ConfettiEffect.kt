package com.example.shouldiorder.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.sp
import com.example.shouldiorder.ui.theme.AppConstants
import kotlin.math.sin
import kotlin.random.Random

data class Confetti(
    val id: Int,
    val emoji: String,
    val startXPercent: Float,
    val startY: Float,
    val delay: Int,
    val duration: Int,
    val rotation: Float = Random.nextFloat() * 360f,
    val horizontalDrift: Float = (Random.nextFloat() - 0.5f) * 150f
)

@Composable
fun ConfettiEffect(
    modifier: Modifier = Modifier,
    isActive: Boolean = false
) {
    if (!isActive) return

    val confettis = remember {
        (0..AppConstants.Confetti.Count).map { i ->
            Confetti(
                id = i,
                emoji = listOf("🍕", "🍔", "🍟", "🌮", "🍗", "🥘")[i % 6],
                startXPercent = Random.nextFloat(),
                startY = -100f + Random.nextFloat() * 50f,
                delay = (i * 8).toInt(),  // Délai minimal (8ms au lieu de 25ms) pour démarrage fluide
                duration = AppConstants.Confetti.DurationMin + Random.nextInt(AppConstants.Confetti.DurationMax - AppConstants.Confetti.DurationMin)
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        confettis.forEach { confetti ->
            ConfettiParticle(confetti)
        }
    }
}

@Composable
private fun ConfettiParticle(confetti: Confetti) {
    val offsetY = remember { Animatable(confetti.startY) }
    val rotation = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }
    val offsetX = remember { Animatable(0f) }

    // Animation fluide de la chute avec easing - démarrage immédiat
    LaunchedEffect(confetti.id) {
        if (confetti.delay > 0) {
            kotlinx.coroutines.delay(confetti.delay.toLong())
        }
        offsetY.animateTo(
            targetValue = 1600f,
            animationSpec = tween(
                durationMillis = confetti.duration,
                easing = CubicBezierEasing(0.25f, 0.46f, 0.45f, 0.94f)
            )
        )
    }

    // Rotation fluide - utilise une clé différente pour paralléliser
    LaunchedEffect("rotation_${confetti.id}") {
        if (confetti.delay > 0) {
            kotlinx.coroutines.delay(confetti.delay.toLong())
        }
        rotation.animateTo(
            targetValue = 360f * (2 + Random.nextFloat()),
            animationSpec = tween(
                durationMillis = confetti.duration,
                easing = CubicBezierEasing(0.33f, 0.66f, 0.66f, 1.0f)
            )
        )
    }

    // Mouvement horizontal progressif - clé unique
    LaunchedEffect("offsetX_${confetti.id}") {
        if (confetti.delay > 0) {
            kotlinx.coroutines.delay(confetti.delay.toLong())
        }
        offsetX.animateTo(
            targetValue = confetti.horizontalDrift,
            animationSpec = tween(
                durationMillis = confetti.duration,
                easing = CubicBezierEasing(0.17f, 0.67f, 0.83f, 0.67f)
            )
        )
    }

    // Fade out progressif - clé unique
    LaunchedEffect("alpha_${confetti.id}") {
        kotlinx.coroutines.delay((confetti.delay + confetti.duration - AppConstants.Confetti.FadeOutDuration).toLong())
        alpha.animateTo(0f, animationSpec = tween(AppConstants.Confetti.FadeOutDuration.toInt()))
    }

    Layout(
        content = {
            Text(
                text = confetti.emoji,
                fontSize = 32.sp,
                modifier = Modifier
                    .graphicsLayer {
                        translationX = (sin(offsetY.value.toDouble() / 70) * 50).toFloat() + offsetX.value
                        translationY = offsetY.value
                        rotationZ = rotation.value
                        this.alpha = alpha.value
                    }
            )
        }
    ) { measurables, constraints ->
        val placeable = measurables[0].measure(constraints)
        layout(constraints.maxWidth, constraints.maxHeight) {
            val xPos = (confetti.startXPercent * constraints.maxWidth).toInt()
            placeable.placeRelative(xPos, 0)
        }
    }
}

