package com.example.my_financialtracker.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.lerp

@Composable
fun AmbientBackground(
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val transition = rememberInfiniteTransition(label = "ambient_background")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "drift",
    )
    val tide by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "tide",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(colorScheme.background)

        val primaryGlow = colorScheme.primary.copy(alpha = if (colorScheme.background.red < 0.2f) 0.12f else 0.10f)
        val secondaryGlow = colorScheme.secondary.copy(alpha = if (colorScheme.background.red < 0.2f) 0.10f else 0.08f)
        val tertiaryGlow = lerp(colorScheme.primary, colorScheme.secondary, 0.45f).copy(
            alpha = if (colorScheme.background.red < 0.2f) 0.08f else 0.06f,
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primaryGlow, primaryGlow.copy(alpha = 0f)),
                center = Offset(size.width * (0.15f + 0.12f * drift), size.height * (0.12f + 0.05f * tide)),
                radius = size.minDimension * 0.55f,
            ),
            radius = size.minDimension * 0.55f,
            center = Offset(size.width * (0.15f + 0.12f * drift), size.height * (0.12f + 0.05f * tide)),
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(secondaryGlow, secondaryGlow.copy(alpha = 0f)),
                center = Offset(size.width * (0.92f - 0.18f * tide), size.height * (0.26f + 0.10f * drift)),
                radius = size.minDimension * 0.48f,
            ),
            radius = size.minDimension * 0.48f,
            center = Offset(size.width * (0.92f - 0.18f * tide), size.height * (0.26f + 0.10f * drift)),
        )

        val topWave = Path().apply {
            moveTo(0f, size.height * 0.18f)
            quadraticBezierTo(
                size.width * 0.22f,
                size.height * (0.10f + 0.03f * drift),
                size.width * 0.48f,
                size.height * (0.17f - 0.02f * tide),
            )
            quadraticBezierTo(
                size.width * 0.74f,
                size.height * (0.25f + 0.03f * tide),
                size.width,
                size.height * (0.16f + 0.02f * drift),
            )
            lineTo(size.width, 0f)
            lineTo(0f, 0f)
            close()
        }

        val bottomWave = Path().apply {
            moveTo(0f, size.height)
            lineTo(0f, size.height * 0.82f)
            quadraticBezierTo(
                size.width * 0.24f,
                size.height * (0.74f - 0.03f * tide),
                size.width * 0.52f,
                size.height * (0.84f + 0.02f * drift),
            )
            quadraticBezierTo(
                size.width * 0.80f,
                size.height * (0.92f - 0.02f * drift),
                size.width,
                size.height * (0.80f + 0.02f * tide),
            )
            lineTo(size.width, size.height)
            close()
        }

        drawPath(
            path = topWave,
            brush = Brush.verticalGradient(
                colors = listOf(tertiaryGlow, tertiaryGlow.copy(alpha = 0f)),
            ),
            style = Fill,
        )
        drawPath(
            path = bottomWave,
            brush = Brush.verticalGradient(
                colors = listOf(tertiaryGlow.copy(alpha = tertiaryGlow.alpha * 0.6f), tertiaryGlow.copy(alpha = 0f)),
            ),
            style = Fill,
        )
    }
}
