package com.indus.veena.ui.screens.player

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Forward
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.ripple.createRippleModifierNode
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.WavyProgressIndicatorDefaults.trackColor
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.palette.graphics.Palette
import coil3.BitmapImage
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.request.crossfade
import com.indus.veena.database.sqlite.entities.DownloadState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.io.path.Path
import kotlin.random.Random
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.indus.veena.database.sqlite.entities.DownloadEntity
import com.indus.veena.models.SongModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FullPlayerOverlay(
    state: PlayerViewModel.PlayerState,
    viewModel: PlayerViewModel,
    onCollapse: () -> Unit
) {
    val song = state.activeSong ?: return
    val context = LocalContext.current
    val currentPosition by viewModel.currentPosition.collectAsStateWithLifecycle()
    val downloads by viewModel.activeDownloads.collectAsStateWithLifecycle(emptyList())
    val downloadEntity = downloads.find { it.songId == state.activeSong.id }

    // Lifecycle observer for animations
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsStateWithLifecycle()
    val isAnimActive = lifecycleState.isAtLeast(Lifecycle.State.RESUMED)

    // Background color extraction (Non-blocking)
    LaunchedEffect(song.thumbnail) {
        withContext(Dispatchers.IO) {
            val color = extractColorsFromBitmap(context, song.thumbnail)
            withContext(Dispatchers.Main) { viewModel.updateDominantColor(color) }
        }
    }

    if (state.displayMode == PlayerViewModel.PlayerDisplayMode.FULL) {
        BackHandler { onCollapse() }
    }

    PlayerSheetContent(
        song = song,
        state = state,
        currentPosition = currentPosition,
        downloadEntity = downloadEntity,
        isAnimActive = isAnimActive,
        onCollapse = onCollapse,
        onSeek = viewModel::seekTo,
        onTogglePlayPause = viewModel::togglePlayPause,
        onStop = { viewModel.stopPlayer(); onCollapse() },
        onDownload = viewModel::downloadCurrentSong
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PlayerSheetContent(
    song: SongModel,
    state: PlayerViewModel.PlayerState,
    currentPosition: Long,
    downloadEntity: DownloadEntity?,
    isAnimActive: Boolean,
    onCollapse: () -> Unit,
    onSeek: (Long) -> Unit,
    onTogglePlayPause: () -> Unit,
    onStop: () -> Unit,
    onDownload: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val offsetY = remember { Animatable(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = (offsetY.value + dragAmount).coerceAtLeast(-50f)
                        coroutineScope.launch { offsetY.snapTo(newOffset) }
                    },
                    onDragEnd = {
                        if (offsetY.value > size.height * 0.25f) onCollapse()
                        else coroutineScope.launch { offsetY.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow)) }
                    }
                )
            }
            .graphicsLayer { translationY = offsetY.value.coerceAtLeast(0f) }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .border(1.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
        ) {
            // Background Gradients
            val gradientColors = remember(state.dominantColor) {
                listOf(state.dominantColor.copy(alpha = 0.95f), state.dominantColor.darken(0.45f), Color(0xFF0D0D0D))
            }
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = gradientColors)))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(song.thumbnail).crossfade(true).build(),
                    contentDescription = null, contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().graphicsLayer { scaleX = 1.4f; scaleY = 1.4f; clip = false }.blur(72.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                )
            }

            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(0.35f), Color.Black.copy(0.75f)))))

            Column(
                modifier = Modifier.fillMaxSize().navigationBarsPadding().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onCollapse) {
                        Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White)
                    }

                    // Song/Video Switcher
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                            .padding(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text("Song", color = Color.White, style = MaterialTheme.typography.labelMedium)
                            }
                            Text(
                                "Video",
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .clickable {
                                        val query = Uri.encode("${song.title} ${song.artist}")
                                        val uri =
                                            "https://www.youtube.com/results?search_query=$query".toUri()
                                        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                            // Prefer YouTube app if installed, falls back to browser
                                            setPackage("com.google.android.youtube")
                                        }
                                        // If YouTube app not found, open in browser
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: ActivityNotFoundException) {
                                            context.startActivity(
                                                Intent(Intent.ACTION_VIEW, uri)
                                            )
                                        }
                                    },
                                color = Color.White.copy(alpha = 0.5f),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    IconButton(onClick = {
                        onStop()
                        onCollapse()
                    }) {
                        Icon(Icons.Default.Close, "Stop", tint = Color.White)
                    }
                }

                Spacer(Modifier.weight(0.1f))

                // Artwork with Lifecycle-Aware Breathing
                val artworkScale by animateFloatAsState(
                    targetValue = if (state.isPlaying && isAnimActive) 1.03f else 1.0f,
                    animationSpec = if (state.isPlaying && isAnimActive) infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse)
                    else tween(400, easing = FastOutSlowInEasing), label = "artScale"
                )

                Box(
                    modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .aspectRatio(1f)
                            .graphicsLayer { scaleX = artworkScale; scaleY = artworkScale }
                            .shadow(32.dp, RoundedCornerShape(24.dp),
                                ambientColor = state.dominantColor!!)
                            .clip(RoundedCornerShape(24.dp))
                ) {
                    AsyncImage(model = song.thumbnail, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                }

                Spacer(Modifier.weight(0.1f))

                // Text & Download
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    AnimatedContent(
                        contentAlignment = Alignment.Center,
                        targetState = when {
                            // Case 1: Just started or waiting for WorkManager
                            downloadEntity?.state == DownloadState.FETCHING ||
                                    downloadEntity?.state == DownloadState.PENDING -> "BUSY_INDETERMINATE"
                            // Case 2: Done
                            downloadEntity?.state == DownloadState.COMPLETED -> "COMPLETED"
                            // Case 3: Failed
                            downloadEntity?.state == DownloadState.FAILED -> "IDLE"
                            // Case 4: Actively downloading or processing
                            downloadEntity != null -> "PROGRESS"
                            else -> "IDLE"
                        },
                        transitionSpec = {
                            scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn() togetherWith
                                    scaleOut(spring(dampingRatio = Spring.DampingRatioNoBouncy)) + fadeOut()
                        },
                        label = "DownloadState"
                    ) { state ->
                        when (state) {
                            "BUSY_INDETERMINATE" -> {
                                // Indeterminate bar for Fetching/Pending
                                CircularProgressIndicator(
                                    modifier = Modifier.padding(12.dp).size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            "COMPLETED" -> {
                                IconButton(onClick = { /* redownload dialog */ }) {
                                    Icon(
                                        Icons.Rounded.CheckCircle,
                                        contentDescription = "Downloaded",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            "PROGRESS" -> {
                                val progressValue = (downloadEntity?.progress ?: 0) / 100f
                                val isProcessing =
                                    progressValue >= 0.99f && downloadEntity?.state != DownloadState.COMPLETED

                                Box(contentAlignment = Alignment.Center) {
                                    val animatedProgress by animateFloatAsState(
                                        targetValue = progressValue,
                                        animationSpec = if (progressValue == 0f) snap() else tween(
                                            500,
                                            easing = LinearEasing
                                        ),
                                        label = "progress"
                                    )

                                    if (isProcessing) {
                                        // Indeterminate Wavy for Tagging/Encoding phase
                                        CircularWavyProgressIndicator(
                                            modifier = Modifier.size(46.dp),
                                            stroke = with(LocalDensity.current) { Stroke(3f) },
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        // Determinate Wavy for active byte downloading
                                        CircularWavyProgressIndicator(
                                            progress = { animatedProgress },
                                            modifier = Modifier.size(46.dp),
                                            stroke = with(LocalDensity.current) { Stroke(3f) },
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            else -> { // IDLE
                                IconButton(onClick = { onDownload() }) {
                                    Icon(Icons.Default.Download, contentDescription = "Download")
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // OPTIMIZED SEEKBAR (Fixed disappearing bug + Added Pill)
                val progress = if (state.duration > 0) currentPosition.toFloat() / state.duration.toFloat() else 0f
                var isDragging by remember { mutableStateOf(false) }
                var dragProgress by remember { mutableFloatStateOf(progress) }

                LaunchedEffect(progress) { if (!isDragging) dragProgress = progress }
                val displayProgress = if (isDragging) dragProgress else progress

                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth().height(24.dp), contentAlignment = Alignment.Center) {
                        LinearWavyProgressIndicator(
                            progress = { displayProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .pointerInput(state.duration) {
                                    detectTapGestures { offset ->
                                        val seekFraction = (offset.x / size.width).coerceIn(0f, 1f)
                                        onSeek((seekFraction * state.duration).toLong())
                                    }
                                }
                                .pointerInput(state.duration) {
                                    detectHorizontalDragGestures(
                                        onDragStart = { isDragging = true },
                                        onDragEnd = { isDragging = false; onSeek((dragProgress * state.duration).toLong()) },
                                        onDragCancel = { isDragging = false },
                                        onHorizontalDrag = { change, _ ->
                                            change.consume()
                                            dragProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                                        }
                                    )
                                },
                            color = if (state.bufferState) Color.White.copy(alpha = 0.5f) else Color.White,
                            waveSpeed = 20.dp,
                            trackColor = Color.White.copy(alpha = 0.2f),
                            amplitude = { if (state.isPlaying && !isDragging && !state.bufferState) 1f else 0f }
                        )

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val thumbX = displayProgress * size.width
                            val pillWidth = 4.dp.toPx()   // Thinner width
                            val pillHeight = 20.dp.toPx() // Vertical height

                            drawRoundRect(
                                color = Color.White,
                                topLeft = Offset(
                                    x = thumbX - (pillWidth / 2f),
                                    y = (size.height / 2f) - (pillHeight / 2f)
                                ),
                                size = Size(pillWidth, pillHeight),
                                cornerRadius = CornerRadius(pillWidth / 2f) // Makes it perfectly rounded (a pill)
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(formatTime(currentPosition), color = Color.White.copy(0.6f), style = MaterialTheme.typography.labelSmall)
                        Text(formatTime(state.duration), color = Color.White.copy(0.6f), style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(Modifier.weight(0.1f))

                // CONTROLS (Snappier LED colors & Integrated Buffering Spinner)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    MetallicSeekButton(icon = Icons.Rounded.Replay10, onClick = { onSeek(currentPosition - 10000) })

                    Box(contentAlignment = Alignment.Center) {
                        MetallicPlayPauseButton(
                            isPlaying = state.isPlaying,
                            onClick = onTogglePlayPause,
                            isAnimActive = isAnimActive,
                            baseColor = Color(0xFF252525)
                        )
                    }

                    MetallicSeekButton(icon = Icons.Rounded.Forward10, onClick = { onSeek(currentPosition + 10000) })
                }
                Spacer(Modifier.weight(0.1f))
            }
        }
    }
}

@Composable
fun MetallicPlayPauseButton(
    isPlaying: Boolean,
    isAnimActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    baseColor: Color = Color(0xFF2C2C2C)
) {
    val ringColors = rememberMetallicRings(baseColor)
    val transition = rememberInfiniteTransition(label = "shimmer")
    val rotation by transition.animateFloat(
        initialValue = 0f, targetValue = if (isAnimActive) 360f else 0f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing)), label = "rot"
    )

    // Snappier LED transition (removed the 500ms tween that looked bad)
    val ledColor by animateColorAsState(
        targetValue = if (isPlaying) Color(0xFF00FF88) else Color(0xFFFF3344),
        animationSpec = tween(150), label = "led"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = modifier
                .size(82.dp)
                .metallicSurface(baseColor, ringColors, rotation)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            // Snappy icon switch
            Icon(
                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = null, modifier = Modifier.size(38.dp), tint = Color.White.copy(alpha = 0.9f)
            )
        }
        Spacer(Modifier.height(12.dp))
        Box(modifier = Modifier.size(6.dp, 4.dp).background(ledColor, RoundedCornerShape(2.dp)).shadow(12.dp, CircleShape, spotColor = ledColor, ambientColor = ledColor))
    }
}
/*
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FullPlayerOverlay1(
    state: PlayerViewModel.PlayerState,
    viewModel: PlayerViewModel,
    onCollapse: () -> Unit
) {
    val song = state.activeSong ?: return
    val context = LocalContext.current

    // 1. Dynamic Background & State
    var dominantColor by remember { mutableStateOf(Color(0xFF1A1616)) }
    val isPlaying = state.isPlaying
    val downloads by viewModel.activeDownloads.collectAsStateWithLifecycle(emptyList())
    val downloadEntity = downloads.find { it.songId == state.activeSong.id }

    LaunchedEffect(song.thumbnail) {
        withContext(Dispatchers.IO) {
            val color = extractColorsFromBitmap(context, song.thumbnail)
            withContext(Dispatchers.Default) {
                dominantColor = color
                viewModel.updateDominantColor(color)
            }
        }
    }

    if (state.displayMode == PlayerViewModel.PlayerDisplayMode.FULL) {
        BackHandler { onCollapse() }
    }

    // 2. Advanced Gesture Handling
    val coroutineScope = rememberCoroutineScope()
    val offsetY = remember { Animatable(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = (offsetY.value + dragAmount).coerceAtLeast(-50f)
                        coroutineScope.launch { offsetY.snapTo(newOffset) }
                    },
                    onDragEnd = {
                        if (offsetY.value > size.height * 0.25f) {
                            onCollapse()
                        } else {
                            coroutineScope.launch {
                                offsetY.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                            }
                        }
                    }
                )
            }
            .graphicsLayer { translationY = offsetY.value.coerceAtLeast(0f) }
    ) {
        // Main Container
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .border(1.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
        ) {
            // 1. STATIC GRADIENT LAYER (Always visible behind the image)
            val gradientColors = remember(dominantColor) {
                listOf(
                    dominantColor.copy(alpha = 0.95f),
                    dominantColor.darken(0.45f).copy(alpha = 1f),
                    Color(0xFF0D0D0D)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(colors = gradientColors))
            )

            // 2. IMAGE BLUR LAYER (Fades in over the gradient)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(song.thumbnail)
                        .crossfade(true) // Smooth transition from transparent to image
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = 1.4f
                            scaleY = 1.4f
                            clip = false
                        }
                        .blur(72.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                )
            }

            // Dark gradient overlay — same for both paths, ensures readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.35f),
                                Color.Black.copy(alpha = 0.75f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onCollapse) {
                        Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White)
                    }

                    // Song/Video Switcher
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                            .padding(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text("Song", color = Color.White, style = MaterialTheme.typography.labelMedium)
                            }
                            Text(
                                "Video",
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .clickable {
                                        val query = Uri.encode("${song.title} ${song.artist}")
                                        val uri =
                                            "https://www.youtube.com/results?search_query=$query".toUri()
                                        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                            // Prefer YouTube app if installed, falls back to browser
                                            setPackage("com.google.android.youtube")
                                        }
                                        // If YouTube app not found, open in browser
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: ActivityNotFoundException) {
                                            context.startActivity(
                                                Intent(Intent.ACTION_VIEW, uri)
                                            )
                                        }
                                    },
                                color = Color.White.copy(alpha = 0.5f),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    IconButton(onClick = {
                        viewModel.stopPlayer()
                        onCollapse()
                    }) {
                        Icon(Icons.Default.Close, "Stop", tint = Color.White)
                    }
                }

                Spacer(Modifier.weight(0.1f))

                // ARTWORK
                // Scale pulses 1.0 → 1.03 → 1.0 while playing; static when paused
                val artworkScale by animateFloatAsState(
                    targetValue = if (isPlaying) 1.03f else 1.0f,
                    animationSpec = if (isPlaying) {
                        infiniteRepeatable(
                            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    } else {
                        tween(durationMillis = 400, easing = FastOutSlowInEasing)
                    },
                    label = "artworkBreath"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .aspectRatio(1f)
                        .graphicsLayer {
                            scaleX = artworkScale
                            scaleY = artworkScale
                        }
                        .shadow(32.dp, RoundedCornerShape(24.dp), ambientColor = dominantColor)
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    AsyncImage(
                        model = song.thumbnail,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(Modifier.weight(0.1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    AnimatedContent(
                        contentAlignment = Alignment.Center,
                        targetState = when {
                            // Case 1: Just started or waiting for WorkManager
                            downloadEntity?.state == DownloadState.FETCHING ||
                                    downloadEntity?.state == DownloadState.PENDING -> "BUSY_INDETERMINATE"
                            // Case 2: Done
                            downloadEntity?.state == DownloadState.COMPLETED -> "COMPLETED"
                            // Case 3: Failed
                            downloadEntity?.state == DownloadState.FAILED -> "IDLE"
                            // Case 4: Actively downloading or processing
                            downloadEntity != null -> "PROGRESS"
                            else -> "IDLE"
                        },
                        transitionSpec = {
                            scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn() togetherWith
                                    scaleOut(spring(dampingRatio = Spring.DampingRatioNoBouncy)) + fadeOut()
                        },
                        label = "DownloadState"
                    ) { state ->
                        when (state) {
                            "BUSY_INDETERMINATE" -> {
                                // Indeterminate bar for Fetching/Pending
                                CircularProgressIndicator(
                                    modifier = Modifier.padding(12.dp).size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            "COMPLETED" -> {
                                IconButton(onClick = { *//* redownload dialog *//* }) {
                                    Icon(
                                        Icons.Rounded.CheckCircle,
                                        contentDescription = "Downloaded",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            "PROGRESS" -> {
                                val progressValue = (downloadEntity?.progress ?: 0) / 100f
                                val isProcessing =
                                    progressValue >= 0.99f && downloadEntity?.state != DownloadState.COMPLETED

                                Box(contentAlignment = Alignment.Center) {
                                    val animatedProgress by animateFloatAsState(
                                        targetValue = progressValue,
                                        animationSpec = if (progressValue == 0f) snap() else tween(
                                            500,
                                            easing = LinearEasing
                                        ),
                                        label = "progress"
                                    )

                                    if (isProcessing) {
                                        // Indeterminate Wavy for Tagging/Encoding phase
                                        CircularWavyProgressIndicator(
                                            modifier = Modifier.size(46.dp),
                                            stroke = with(LocalDensity.current) { Stroke(3f) },
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        // Determinate Wavy for active byte downloading
                                        CircularWavyProgressIndicator(
                                            progress = { animatedProgress },
                                            modifier = Modifier.size(46.dp),
                                            stroke = with(LocalDensity.current) { Stroke(3f) },
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            else -> { // IDLE
                                IconButton(onClick = viewModel::downloadCurrentSong) {
                                    Icon(Icons.Default.Download, contentDescription = "Download")
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // PROGRESS
                val progress = if (state.duration > 0)
                    state.currentPosition.toFloat() / state.duration.toFloat() else 0f

                Column(modifier = Modifier.fillMaxWidth()) {
                    if (state.bufferState) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f),
                            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                        )
                    } else {
                        // Combined tap + drag seek
                        var isDragging by remember { mutableStateOf(false) }
                        var dragProgress by remember { mutableFloatStateOf(progress) }

                        // Keep drag progress synced when not actively dragging
                        LaunchedEffect(progress, isDragging) {
                            if (!isDragging) dragProgress = progress
                        }

                        val displayProgress = if (isDragging) dragProgress else progress

                        LinearWavyProgressIndicator(
                            progress = { displayProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .pointerInput(state.duration) {
                                    // Tap to seek
                                    detectTapGestures { offset ->
                                        val seekFraction = (offset.x / size.width).coerceIn(0f, 1f)
                                        viewModel.seekTo((seekFraction * state.duration).toLong())
                                    }
                                }
                                .pointerInput(state.duration) {
                                    // Drag to seek
                                    detectHorizontalDragGestures(
                                        onDragStart = { isDragging = true },
                                        onDragEnd = {
                                            isDragging = false
                                            viewModel.seekTo((dragProgress * state.duration).toLong())
                                        },
                                        onDragCancel = {
                                            isDragging = false
                                        },
                                        onHorizontalDrag = { change, _ ->
                                            change.consume()
                                            val newFraction = (change.position.x / size.width).coerceIn(0f, 1f)
                                            dragProgress = newFraction
                                        }
                                    )
                                },
                            color = Color.White,
                            waveSpeed = 20.dp,
                            trackColor = Color.White.copy(alpha = 0.2f),
                            amplitude = { if (state.isPlaying && !isDragging) 1f else 0f }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formatTime(state.currentPosition), color = Color.White.copy(0.6f), style = MaterialTheme.typography.labelSmall)
                        Text(formatTime(state.duration), color = Color.White.copy(0.6f), style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(Modifier.weight(0.1f))

                // CONTROLS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MetallicSeekButton(
                        icon = Icons.Rounded.Replay10,
                        onClick = { viewModel.seekTo(state.currentPosition - 10000) }
                    )

// The Main Metallic Play/Pause
                    MetallicPlayPauseButton(
                        isPlaying = isPlaying,
                        onClick = viewModel::togglePlayPause,
                        baseColor = Color(0xFF252525) // Deep charcoal metal
                    )

// Forward 10s
                    MetallicSeekButton(
                        icon = Icons.Rounded.Forward10,
                        onClick = { viewModel.seekTo(state.currentPosition + 10000) }
                    )
                }

                Spacer(Modifier.weight(0.1f))
            }
        }
    }
}*/

private fun Color.darken(fraction: Float): Color {
    return Color(
        red = (red * (1f - fraction)).coerceIn(0f, 1f),
        green = (green * (1f - fraction)).coerceIn(0f, 1f),
        blue = (blue * (1f - fraction)).coerceIn(0f, 1f),
        alpha = alpha
    )
}

@Composable
fun rememberMetallicRings(baseColor: Color): List<Color> {
    return remember(baseColor) {
        val ringColor = lerp(baseColor, Color.Black, 0.4f).copy(alpha = 0.3f)
        val rng = Random(baseColor.hashCode())
        buildList {
            repeat(40) {
                repeat(rng.nextInt(2, 12)) { add(Color.Transparent) }
                repeat(rng.nextInt(1, 3)) { add(ringColor) }
            }
        }
    }
}

@Composable
fun MetallicSeekButton(
    icon: ImageVector,
    onClick: () -> Unit,
    baseColor: Color = Color(0xFF3A3A3A)
) {
    val ringColors = rememberMetallicRings(baseColor)

    Box(
        modifier = Modifier
            .size(56.dp)
            .metallicSurface(
                baseColor = baseColor,
                ringColors = ringColors,
                highlightRotation = 45f   // fixed angle — static sheen, no spinning
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, modifier = Modifier.size(24.dp), tint = Color.White.copy(0.85f))
    }
}

fun Modifier.metallicSurface(
    baseColor: Color = Color(0xFF9A9A9A),
    ringColors: List<Color>,
    highlightRotation: Float,
    shape: Shape = CircleShape
) = this.then(
    Modifier
        .shadow(8.dp, shape)
        .border(
            width = 1.dp,
            shape = shape,
            brush = Brush.verticalGradient(
                listOf(Color.White.copy(0.5f), Color.Transparent)
            )
        )
        .drawBehind {
            val highlightColor = lerp(baseColor, Color.White, 0.5f).copy(alpha = 0.4f)
            val highlightColors = listOf(
                highlightColor.copy(alpha = 0f),
                highlightColor,
                highlightColor.copy(alpha = 0f),
                highlightColor,
                highlightColor.copy(alpha = 0f)
            )

            val path = Path().apply {
                addOutline(shape.createOutline(size, layoutDirection, this@drawBehind))
            }

            clipPath(path) {
                // 1. Base Layer
                drawRect(baseColor)

                // 2. Brushed Rings
                drawRect(
                    brush = Brush.radialGradient(
                        colors = ringColors,
                        tileMode = TileMode.Repeated,
                    ),
                    blendMode = BlendMode.Overlay,
                )

                // 3. Rotating Light Shimmer
                rotate(highlightRotation) {
                    drawCircle(
                        brush = Brush.sweepGradient(colors = highlightColors),
                        radius = size.width * 1.5f
                    )
                }
            }
        }
)

@Composable
fun MetallicPlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    baseColor: Color = Color(0xFF2C2C2C) // Darker metal looks better for play buttons
) {
    val ringColors = rememberMetallicRings(baseColor)
    val transition = rememberInfiniteTransition(label = "shimmer")
    val rotation by transition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing)), label = "rot"
    )

    // LED Color Animation
    val ledColor by animateColorAsState(
        targetValue = if (isPlaying) Color(0xFF00FF88) else Color(0xFFFF3344),
        animationSpec = tween(500), label = "led"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = modifier
                .size(82.dp)
                .metallicSurface(baseColor, ringColors, rotation)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    //indication = createRippleModifierNode(interactionSource, bounded = true, radius = 40.dp),
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(38.dp),
                tint = Color.White.copy(alpha = 0.9f)
            )
        }

        Spacer(Modifier.height(12.dp))

        // The Status LED
        Box(
            modifier = Modifier
                .size(6.dp, 4.dp)
                .background(ledColor, RoundedCornerShape(2.dp))
                .shadow(elevation = 12.dp, shape = CircleShape, spotColor = ledColor, ambientColor = ledColor)
        )
    }
}

suspend fun extractColorsFromBitmap(context: PlatformContext, imageUrl: String): Color {
    val loader = ImageLoader(context)
    val request = ImageRequest.Builder(context)
        .data(imageUrl)
        .allowHardware(false)
        .build()

    val result = loader.execute(request)
    return if (result is SuccessResult) {
        val bitmap = (result.image as? BitmapImage)?.bitmap
        if (bitmap != null) {
            val palette = Palette.from(bitmap).generate()
            val swatch = palette.mutedSwatch ?: palette.dominantSwatch
            swatch?.rgb?.let { Color(it) } ?: Color(0xFF1A1616)
        } else Color(0xFF1A1616)
    } else Color(0xFF1A1616)
}

fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
