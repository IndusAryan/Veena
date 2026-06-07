package com.indus.veena.ui.screens.splash

import android.app.Activity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.indus.veena.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// ─────────────────────────────────────────────────────────────
//  VolumetricSplashScreen
//  Cinematic 5-phase logo reveal:
//   0 → Darkness / ambient pulse
//   1 → God rays sweep + logo edge scatter
//   2 → Ray bloom peak
//   3 → Rays fade, crisp white logo materialises
//   4 → Hold → exit
// ─────────────────────────────────────────────────────────────
@Composable
fun VolumetricSplashScreen(onSplashComplete: () -> Unit) {

    val view = LocalView.current
    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window ?: return@DisposableEffect onDispose {}
        val controller = WindowCompat.getInsetsController(window, view)

        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        onDispose {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    // ── Palette ──────────────────────────────────────────────
    val black       = Color(0xFF000000)
    val deepPurple  = Color(0xFF4B0082)
    val violet      = Color(0xFFAA44FF)
    val lavender    = Color(0xFFD8BFD8)
    val pureWhite   = Color(0xFFFFFFFF)

    // ── Phase tracker (0‥4) ──────────────────────────────────
    var phase by remember { mutableIntStateOf(0) }

    // ── Per-ray independent rotation ─────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "rays")

    val rayDefs = remember {
        listOf(
            Pair(4200,   0f),
            Pair(5800,  47f),
            Pair(3700,  112f),
            Pair(6500,  188f),
            Pair(4900,  230f),
            Pair(3200,  290f),
            Pair(7100,  340f),
            Pair(5100,  75f),
            Pair(4600,  155f),
        )
    }

    val ray0 by infiniteTransition.animateFloat(0f + rayDefs[0].second, 360f + rayDefs[0].second, infiniteRepeatable(tween(rayDefs[0].first, easing = LinearEasing), RepeatMode.Restart), label = "r0")
    val ray1 by infiniteTransition.animateFloat(0f + rayDefs[1].second, 360f + rayDefs[1].second, infiniteRepeatable(tween(rayDefs[1].first, easing = LinearEasing), RepeatMode.Restart), label = "r1")
    val ray2 by infiniteTransition.animateFloat(0f + rayDefs[2].second, 360f + rayDefs[2].second, infiniteRepeatable(tween(rayDefs[2].first, easing = LinearEasing), RepeatMode.Restart), label = "r2")
    val ray3 by infiniteTransition.animateFloat(0f + rayDefs[3].second, 360f + rayDefs[3].second, infiniteRepeatable(tween(rayDefs[3].first, easing = LinearEasing), RepeatMode.Restart), label = "r3")
    val ray4 by infiniteTransition.animateFloat(0f + rayDefs[4].second, 360f + rayDefs[4].second, infiniteRepeatable(tween(rayDefs[4].first, easing = LinearEasing), RepeatMode.Restart), label = "r4")
    val ray5 by infiniteTransition.animateFloat(0f + rayDefs[5].second, 360f + rayDefs[5].second, infiniteRepeatable(tween(rayDefs[5].first, easing = LinearEasing), RepeatMode.Restart), label = "r5")
    val ray6 by infiniteTransition.animateFloat(0f + rayDefs[6].second, 360f + rayDefs[6].second, infiniteRepeatable(tween(rayDefs[6].first, easing = LinearEasing), RepeatMode.Restart), label = "r6")
    val ray7 by infiniteTransition.animateFloat(0f + rayDefs[7].second, 360f + rayDefs[7].second, infiniteRepeatable(tween(rayDefs[7].first, easing = LinearEasing), RepeatMode.Restart), label = "r7")
    val ray8 by infiniteTransition.animateFloat(0f + rayDefs[8].second, 360f + rayDefs[8].second, infiniteRepeatable(tween(rayDefs[8].first, easing = LinearEasing), RepeatMode.Restart), label = "r8")

    val rayAngles = listOf(ray0, ray1, ray2, ray3, ray4, ray5, ray6, ray7, ray8)

    // ── Phase-driven animatables ──────────────────────────────
    val ambientPulse   = remember { Animatable(0f) }
    val rayIntensity   = remember { Animatable(0f) }
    val bloomScale     = remember { Animatable(0f) }
    val scatterBurst   = remember { Animatable(0f) }
    val rayAlpha       = remember { Animatable(0f) }
    val logoFillAlpha  = remember { Animatable(0f) }
    val deluxeProgress = remember { Animatable(0f) }
    val logoGlowAlpha  = remember { Animatable(0f) }

    val particles = remember {
        List(120) {
            Particle(
                angle  = (it / 120f) * 360f + (Math.random() * 8f).toFloat() - 4f,
                speed  = 0.55f + (Math.random() * 0.65f).toFloat(),
                radius = 2f   + (Math.random() * 3.5f).toFloat(),
                alpha  = 0.6f + (Math.random() * 0.4f).toFloat()
            )
        }
    }

    // ── Density helper ────────────────────────────────────────
    val density = LocalDensity.current

    // ── Optimized Paint & Filter Resources ──────────────────
    val hazeFilter = remember(density) {
        android.graphics.BlurMaskFilter(with(density) { 40.dp.toPx() }, android.graphics.BlurMaskFilter.Blur.NORMAL)
    }
    val coreFilter = remember(density) {
        android.graphics.BlurMaskFilter(with(density) { 14.dp.toPx() }, android.graphics.BlurMaskFilter.Blur.NORMAL)
    }
    val paintHaze = remember { Paint().apply { asFrameworkPaint().isAntiAlias = true } }
    val paintCore = remember { Paint().apply { asFrameworkPaint().isAntiAlias = true } }

    // ── Sequence ─────────────────────────────────────────────
    LaunchedEffect(Unit) {
        // ─ Phase 0: Ambient pulse ─────────
        phase = 0
        launch {
            ambientPulse.animateTo(0.18f, tween(500, easing = EaseInOut))
        }
        delay(300)

        // ─ Phase 1: God rays ignite + Deluxe write-on ───────────
        phase = 1
        launch {
            rayIntensity.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
        }
        launch {
            bloomScale.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
        }
        launch {
            logoGlowAlpha.animateTo(1f, tween(600, easing = EaseIn))
        }
        // Deluxe starts writing just as rays begin to appear
        launch {
            deluxeProgress.animateTo(1f, tween(1100, easing = FastOutSlowInEasing))
        }
        delay(200)
        launch {
            scatterBurst.animateTo(1f, tween(700, easing = EaseOut))
        }
        delay(500)

        // ─ Phase 2: Ray peak – hold ────────────────────────────
        phase = 2
        rayAlpha.snapTo(1f)
        delay(200)

        // ─ Phase 3: Rays fade out, crisp logo materialises ────
        phase = 3
        launch {
            rayIntensity.animateTo(0f, tween(600, easing = EaseIn))
        }
        launch {
            rayAlpha.animateTo(0f, tween(600, easing = EaseIn))
        }
        launch {
            bloomScale.animateTo(0f, tween(600, easing = EaseIn))
        }
        launch {
            logoGlowAlpha.animateTo(0.45f, tween(400, easing = EaseIn))
        }
        launch {
            logoFillAlpha.animateTo(1f, tween(500, easing = EaseOut))
        }
        delay(750)

        // ─ Phase 4: Hold → exit ────────────────────────────────
        phase = 4
        delay(300)
        onSplashComplete()
    }

    // ── Render ────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(black),
        contentAlignment = Alignment.Center
    ) {

        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width  / 2f
            val cy = size.height / 2f
            val maxR = sqrt(cx * cx + cy * cy)

            // ── 1. Ambient vignette pulse ─────────────────────
            if (ambientPulse.value > 0f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            deepPurple.copy(alpha = ambientPulse.value * 0.35f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = maxR * 0.75f
                    ),
                    center = Offset(cx, cy),
                    radius = maxR * 0.75f
                )
            }

            // ── 2. God rays ─────────────────
            if (rayIntensity.value > 0f && rayAlpha.value > 0f) {
                val brightnessLevels = listOf(1f, 0.5f, 0.75f, 0.35f, 0.9f, 0.45f, 0.65f, 0.3f, 0.8f)
                val coneHalfAngles   = listOf(14.0, 8.0, 18.0, 10.0, 12.0, 7.0, 16.0, 9.0, 13.0)

                rayAngles.forEachIndexed { i, angleDeg ->
                    val angleRad     = Math.toRadians(angleDeg.toDouble()).toFloat()
                    val halfRad      = Math.toRadians(coneHalfAngles[i]).toFloat()
                    val brightness   = brightnessLevels[i]
                    val baseAlpha    = brightness * rayIntensity.value * rayAlpha.value * 0.22f

                    val reach   = maxR * 1.5f
                    val leftX   = cx + cos(angleRad + halfRad) * reach
                    val leftY   = cy + sin(angleRad + halfRad) * reach
                    val rightX  = cx + cos(angleRad - halfRad) * reach
                    val rightY  = cy + sin(angleRad - halfRad) * reach

                    val path = Path().apply {
                        moveTo(cx, cy)
                        lineTo(leftX, leftY)
                        lineTo(rightX, rightY)
                        close()
                    }

                    paintHaze.color = violet.copy(alpha = baseAlpha * 0.6f)
                    paintHaze.asFrameworkPaint().maskFilter = hazeFilter
                    paintCore.color = lavender.copy(alpha = baseAlpha * 0.4f)
                    paintCore.asFrameworkPaint().maskFilter = coreFilter

                    drawContext.canvas.drawPath(path, paintHaze)
                    drawContext.canvas.drawPath(path, paintCore)
                }

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            pureWhite.copy(alpha = rayIntensity.value * rayAlpha.value * 0.55f),
                            violet.copy(alpha    = rayIntensity.value * rayAlpha.value * 0.18f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = with(density) { 35.dp.toPx() }
                    ),
                    center = Offset(cx, cy),
                    radius = with(density) { 35.dp.toPx() }
                )
            }

            // ── 3. Scatter particles ──────────────────────────
            if (scatterBurst.value > 0f) {
                val maxDist = maxR * 0.55f * scatterBurst.value
                particles.forEach { p ->
                    val rad    = Math.toRadians(p.angle.toDouble())
                    val dist   = maxDist * p.speed
                    val px     = cx + cos(rad).toFloat() * dist
                    val py     = cy + sin(rad).toFloat() * dist
                    val pAlpha = p.alpha * (1f - scatterBurst.value * 0.7f)
                    if (pAlpha > 0f) {
                        drawCircle(
                            color  = lavender.copy(alpha = pAlpha * rayAlpha.value.coerceAtLeast(0.01f).coerceAtMost(1f)),
                            radius = p.radius,
                            center = Offset(px, py)
                        )
                    }
                }
            }
        }

        // ── Text layer ────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (logoGlowAlpha.value > 0f && bloomScale.value > 0f) {
                    androidx.compose.foundation.text.BasicText(
                        text = AnnotatedString("VEENA"),
                        style = TextStyle(
                            fontSize     = 72.sp,
                            fontWeight   = FontWeight.ExtraBold,
                            letterSpacing = 6.sp,
                            brush = Brush.linearGradient(colors = listOf(pureWhite, violet)),
                            drawStyle = Stroke(width = 10f)
                        ),
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = 1f + bloomScale.value * 0.28f
                                scaleY = 1f + bloomScale.value * 0.28f
                                alpha  = logoGlowAlpha.value
                            }
                            .blur(radius = 22.dp)
                    )
                }

                if (logoFillAlpha.value < 1f) {
                    androidx.compose.foundation.text.BasicText(
                        text = AnnotatedString("VEENA"),
                        style = TextStyle(
                            fontSize      = 72.sp,
                            fontWeight    = FontWeight.ExtraBold,
                            letterSpacing = 6.sp,
                            brush = Brush.linearGradient(colors = listOf(pureWhite, lavender)),
                            drawStyle = Stroke(width = 2.5f)
                        ),
                        modifier = Modifier.graphicsLayer {
                            alpha = (1f - logoFillAlpha.value).coerceIn(0f, 1f)
                        }
                    )
                }

                androidx.compose.foundation.text.BasicText(
                    text = AnnotatedString("VEENA"),
                    style = TextStyle(
                        fontSize      = 72.sp,
                        fontWeight    = FontWeight.ExtraBold,
                        letterSpacing = 6.sp,
                        color         = pureWhite
                    ),
                    modifier = Modifier.graphicsLayer { alpha = logoFillAlpha.value }
                )
            }

            androidx.compose.foundation.text.BasicText(
                text = AnnotatedString("Deluxe"),
                style = TextStyle(
                    fontSize   = 38.sp,
                    fontFamily = FontFamily(Font(R.font.playfair_display_variable)),
                    color      = lavender,
                    textAlign  = TextAlign.End
                ),
                modifier = Modifier
                    .offset(y = (-8).dp)
                    .graphicsLayer { alpha = if (phase >= 1) 1f else 0f }
                    .drawWithContent {
                        clipRect(
                            left   = 0f,
                            top    = 0f,
                            right  = size.width * deluxeProgress.value,
                            bottom = size.height
                        ) { this@drawWithContent.drawContent() }
                    }
            )
        }
    }
}

private data class Particle(
    val angle:  Float,
    val speed:  Float,
    val radius: Float,
    val alpha:  Float
)