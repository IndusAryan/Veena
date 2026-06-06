package com.indus.veena.ui.screens.player

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.io.path.Path
import kotlin.io.path.moveTo
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun WavyProgressBar(
    progress: Float, // 0f to 1f
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val phaseShift = remember { Animatable(0f) }

    // Animate wave movement
    LaunchedEffect(Unit) {
        phaseShift.animateTo(
            targetValue = PI.toFloat() * 2,
            animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing))
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset -> onSeek(offset.x / size.width) }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerY = height / 2
            val waveAmplitude = 8.dp.toPx()
            val waveLength = 40.dp.toPx()

            val path = androidx.compose.ui.graphics.Path()
            path.moveTo(0f, centerY)

            // Draw wavy line up to progress
            val endX = width * progress
            for (x in 0..endX.toInt()) {
                val relativeX = x / waveLength
                val y = centerY + waveAmplitude * sin(relativeX * 2 * PI + phaseShift.value).toFloat()
                path.lineTo(x.toFloat(), y)
            }

            // Draw straight line for remaining
            drawPath(
                path = path,
                color = Color.White,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            drawLine(
                color = Color.White.copy(alpha = 0.3f),
                start = Offset(endX, centerY),
                end = Offset(width, centerY),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )

            drawCircle(
                color = Color.White,
                radius = 6.dp.toPx(),
                center = Offset(endX, centerY + waveAmplitude * sin((endX/waveLength) * 2 * PI + phaseShift.value).toFloat())
            )
        }
    }
}