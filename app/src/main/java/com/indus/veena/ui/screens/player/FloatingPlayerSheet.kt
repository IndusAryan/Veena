package com.indus.veena.ui.screens.player

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.indus.veena.R
import com.indus.veena.database.sqlite.entities.DownloadEntity
import com.indus.veena.database.sqlite.entities.DownloadState
import com.indus.veena.helpers.ImageModuleCoil.extractColorsFromBitmap
import com.indus.veena.models.SongModel
import com.indus.veena.ui.screens.home.NowPlayingBars
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

@Composable
fun FullPlayerOverlay(
    state: PlayerState, viewModel: PlaybackCoordinator, onCollapse: () -> Unit
) {
    val song = state.activeSong ?: return
    val context = LocalContext.current
    val currentPosition by viewModel.currentPosition.collectAsStateWithLifecycle()
    val downloads by viewModel.activeDownloads.collectAsStateWithLifecycle(emptyList())
    val downloadEntity = downloads.find { it.songId == state.activeSong.id }

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsStateWithLifecycle()
    val isAnimActive = lifecycleState.isAtLeast(Lifecycle.State.RESUMED)

    LaunchedEffect(song.thumbnail) {
        withContext(Dispatchers.IO) {
            val color = extractColorsFromBitmap(context, song.thumbnail)
            withContext(Dispatchers.Main) { viewModel.updateDominantColor(color) }
        }
    }

    if (state.displayMode == PlayerDisplayMode.FULL) {
        BackHandler { onCollapse() }
    }

    val queue = state.queue
    val queueListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var collapseDrag by remember { mutableFloatStateOf(0f) }
    var observedQueueSongs by remember { mutableStateOf<List<SongModel>?>(null) }
    var settleJob by remember { mutableStateOf<Job?>(null) }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val playerHeight = maxHeight
        val density = LocalDensity.current
        val initialTop = with(density) { (playerHeight * 0.25f).roundToPx() }
        val collapseThreshold = with(density) { playerHeight.toPx() * 0.25f }
        val maxCollapseDrag = with(density) { playerHeight.toPx() * 0.55f }
        val nestedScrollConnection =
            remember(queueListState, collapseThreshold, maxCollapseDrag, onCollapse) {
                object : NestedScrollConnection {
                    private fun moveSheet(delta: Float): Float {
                        if (settleJob?.isActive == true) {
                            settleJob?.cancel()
                        }
                        val next = (collapseDrag + delta).coerceIn(0f, maxCollapseDrag)
                        val consumed = next - collapseDrag
                        collapseDrag = next
                        return consumed
                    }

                    override fun onPreScroll(
                        available: Offset, source: NestedScrollSource
                    ): Offset {
                        if (source != NestedScrollSource.UserInput || available.y >= 0f || collapseDrag <= 0f) {
                            return Offset.Zero
                        }
                        return Offset(0f, moveSheet(available.y))
                    }

                    override fun onPostScroll(
                        consumed: Offset, available: Offset, source: NestedScrollSource
                    ): Offset {
                        if (source != NestedScrollSource.UserInput || available.y <= 0f) return Offset.Zero
                        return Offset(0f, moveSheet(available.y))
                    }

                    override suspend fun onPreFling(available: Velocity): Velocity {
                        if (collapseDrag <= 0f) return Velocity.Zero
                        val drag = collapseDrag
                        val dismiss =
                            drag >= collapseThreshold || available.y > with(density) { 1000.dp.toPx() }
                        if (dismiss) {
                            onCollapse()
                        } else {
                            settleJob = scope.launch {
                                animate(
                                    drag,
                                    0f,
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                ) { value, _ ->
                                    collapseDrag = value.coerceAtLeast(0f)
                                }
                            }
                        }
                        return Velocity(0f, available.y)
                    }
                }
            }
        Box(
            Modifier
                .fillMaxSize()
                .offset {
                    val backgroundTop = if (queueListState.firstVisibleItemIndex == 0) {
                        (initialTop - queueListState.firstVisibleItemScrollOffset).coerceAtLeast(0)
                    } else 0
                    IntOffset(0, backgroundTop + collapseDrag.toInt())
                }
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))) {
            val gradientColors = remember(state.dominantColor) {
                listOf(
                    lerp(state.dominantColor, Color(0xFF111827), 0.6f),
                    Color(0xFF161522),
                    Color(0xFF0B1019)
                )
            }
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(gradientColors))
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(song.thumbnail)
                        .crossfade(true).build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { scaleX = 1.4f; scaleY = 1.4f; clip = false }
                        .blur(72.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded))
            }
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.35f), Color.Black.copy(alpha = 0.75f)
                            )
                        )
                    )
            )
        }
        LaunchedEffect(queue?.songs, queue?.index) {
            if (queue == null) {
                observedQueueSongs = null
            } else if (observedQueueSongs !== queue.songs) {
                observedQueueSongs = queue.songs
                queueListState.scrollToItem(0)
            } else if (queue.index > 0) {
                queueListState.animateScrollToItem(queue.index + 1)
            }
        }
        LazyColumn(
            Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
                .graphicsLayer { translationY = collapseDrag },
            state = queueListState,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                bottom = WindowInsets.navigationBars.asPaddingValues()
                    .calculateBottomPadding() + 32.dp
            )
        ) {
            item(key = "player") {
                Box(Modifier.height(playerHeight)) {
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
                        onDownload = viewModel::downloadCurrentSong,
                        onToggleFavourite = viewModel::toggleFavourite,
                        onQueueStep = viewModel::moveQueue,
                        onShowQueue = {
                            val focusIndex = (queue?.index ?: 0) + 1
                            scope.launch { queueListState.animateScrollToItem(focusIndex) }
                        })
                }
            }
            if (queue != null) {
                item(key = "queue-title") {
                    Text(
                        text = stringResource(R.string.queue_playing_from, queue.source),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    )
                }
                items(
                    queue.songs.size,
                    key = { queue.songs[it].provider + ":" + queue.songs[it].id }) { index ->
                    val track = queue.songs[index]
                    val current = index == queue.index
                    val accent = lerp(MaterialTheme.colorScheme.primary, Color.White, 0.46f)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !current) { viewModel.selectQueueTrack(index) }
                            .padding(horizontal = 24.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .then(
                                if (current) Modifier.border(
                                    1.dp, accent.copy(alpha = 0.7f), RoundedCornerShape(12.dp)
                                ) else Modifier
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AsyncImage(
                            model = track.thumbnail,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                track.title,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                track.artist,
                                color = Color.White.copy(alpha = 0.65f),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (current) {
                            NowPlayingBars(
                                isPlaying = state.isPlaying,
                                isAnimActive = isAnimActive,
                                color = accent,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerSheetContent(
    song: SongModel,
    state: PlayerState,
    currentPosition: Long,
    downloadEntity: DownloadEntity?,
    isAnimActive: Boolean,
    onCollapse: () -> Unit,
    onSeek: (Long) -> Unit,
    onTogglePlayPause: () -> Unit,
    onStop: () -> Unit,
    onDownload: () -> Unit,
    onToggleFavourite: () -> Unit,
    onQueueStep: (Int) -> Unit,
    onShowQueue: () -> Unit
) {
    val context = LocalContext.current
    // The sheet is always dark: lift the user's accent into the soft tones of the original controls.
    val accentColor = lerp(MaterialTheme.colorScheme.primary, Color.White, 0.46f)
    val thumbColor = lerp(accentColor, Color.White, 0.38f)
    val trackColor = accentColor.copy(alpha = 0.22f)
    var showSongDetails by remember(song.id) { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .border(
                    1.5.dp,
                    Color.White.copy(alpha = 0.1f),
                    RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                )
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
        ) {
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
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    stringResource(R.string.song),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                            Text(
                                stringResource(R.string.video),
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .clickable {
                                        val query = Uri.encode("${song.title} ${song.artist}")
                                        val uri =
                                            "https://www.youtube.com/results?search_query=$query".toUri()
                                        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                            setPackage("com.google.android.youtube")
                                        }
                                        // If YouTube app not found, open in browser
                                        try {
                                            context.startActivity(intent)
                                        } catch (_: ActivityNotFoundException) {
                                            context.startActivity(
                                                Intent(Intent.ACTION_VIEW, uri)
                                            )
                                        }
                                    },
                                color = Color.White.copy(alpha = 0.5f),
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                stringResource(R.string.about),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.07f))
                                    .clickable { showSongDetails = true }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    IconButton(onClick = {
                        onStop()
                        onCollapse()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_power_off),
                            stringResource(R.string.stop),
                            tint = Color.White
                        )
                    }
                }

                Spacer(Modifier.weight(0.1f))

                val recordRotation = remember { Animatable(0f) }
                LaunchedEffect(state.isPlaying, isAnimActive) {
                    if (state.isPlaying && isAnimActive) {
                        while (true) {
                            recordRotation.animateTo(
                                recordRotation.value + 360f, tween(24000, easing = LinearEasing)
                            )
                        }
                    }
                }
                Box(
                    Modifier
                        .fillMaxWidth(0.86f)
                        .aspectRatio(1.15f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    val recordBase = Color(0xFF29292D)
                    val grooves = remember(recordBase) {
                        val random = Random(recordBase.hashCode())
                        buildList {
                            repeat(40) {
                                repeat(random.nextInt(2, 12)) { add(Color.Transparent) }
                                repeat(random.nextInt(1, 3)) { add(Color.Black.copy(alpha = 0.3f)) }
                            }
                        }
                    }
                    Box(
                        Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxWidth(0.74f)
                            .aspectRatio(1f)
                            .graphicsLayer { rotationZ = recordRotation.value }
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .border(
                                1.dp, Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(0.5f), Color.Transparent
                                    )
                                ), CircleShape
                            )
                            .drawWithCache {
                                val rings =
                                    Brush.radialGradient(grooves, tileMode = TileMode.Repeated)
                                val sheen = Brush.sweepGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.White.copy(0.2f),
                                        Color.Transparent,
                                        Color.White.copy(0.2f),
                                        Color.Transparent
                                    )
                                )
                                onDrawBehind {
                                    drawCircle(recordBase)
                                    drawCircle(rings, blendMode = BlendMode.Overlay)
                                    drawCircle(sheen)
                                    drawCircle(
                                        Color(0xFF101014), radius = size.minDimension * 0.15f
                                    )
                                    drawCircle(Color.Black, radius = size.minDimension * 0.025f)
                                }
                            }) {
                        // A tiny rim stamp rides with the record and disappears naturally under the sleeve.
                        Text(
                            stringResource(R.string.veena),
                            color = Color.White.copy(alpha = 0.38f),
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 10.dp)
                                .graphicsLayer { rotationZ = 90f },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 8.sp,
                                letterSpacing = 1.2.sp,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                    AsyncImage(
                        model = song.thumbnail,
                        contentDescription = stringResource(R.string.cover_for, song.title),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth(0.78f)
                            .aspectRatio(1f)
                            .shadow(24.dp, RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF252D3D))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(18.dp))
                    )
                }

                Spacer(Modifier.weight(0.1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val heartScale = remember(song.id) { Animatable(1f) }
                    LaunchedEffect(state.isFavourite) {
                        heartScale.animateTo(if (state.isFavourite) 1.3f else 0.78f, tween(120))
                        heartScale.animateTo(
                            1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                        )
                    }
                    IconButton(onClick = onToggleFavourite, modifier = Modifier.graphicsLayer {
                        scaleX = heartScale.value; scaleY = heartScale.value
                    }) {
                        Icon(
                            if (state.isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (state.isFavourite) stringResource(R.string.remove_favourite) else stringResource(
                                R.string.add_favourite
                            ),
                            tint = if (state.isFavourite) Color(0xFFFF91AF) else Color.White
                        )
                    }

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
                        contentAlignment = Alignment.Center, targetState = when {
                            // Case 1: Just started or waiting for WorkManager
                            downloadEntity?.state == DownloadState.FETCHING || downloadEntity?.state == DownloadState.PENDING -> "BUSY_INDETERMINATE"
                            // Case 2: Done
                            downloadEntity?.state == DownloadState.COMPLETED -> "COMPLETED"
                            // Case 3: Failed
                            downloadEntity?.state == DownloadState.FAILED -> "IDLE"
                            // Case 4: Actively downloading or processing
                            downloadEntity != null -> "PROGRESS"
                            else -> "IDLE"
                        }, transitionSpec = {
                            scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn() togetherWith scaleOut(
                                spring(dampingRatio = Spring.DampingRatioNoBouncy)
                            ) + fadeOut()
                        }, label = "DownloadState"
                    ) { state ->
                        when (state) {
                            "BUSY_INDETERMINATE" -> {
                                // Indeterminate bar for Fetching/Pending
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = accentColor
                                )
                            }

                            "COMPLETED" -> {
                                IconButton(onClick = { /* redownload dialog */ }) {
                                    Icon(
                                        Icons.Rounded.CheckCircle,
                                        contentDescription = stringResource(R.string.downloaded),
                                        tint = accentColor,
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
                                            500, easing = LinearEasing
                                        ),
                                        label = "progress"
                                    )

                                    if (isProcessing) {
                                        // Indeterminate Wavy for Tagging/Encoding phase
                                        CircularWavyProgressIndicator(
                                            modifier = Modifier.size(46.dp),
                                            stroke = Stroke(3f),
                                            trackColor = trackColor,
                                            color = accentColor
                                        )
                                    } else {
                                        // Determinate Wavy for active byte downloading
                                        CircularWavyProgressIndicator(
                                            progress = { animatedProgress },
                                            modifier = Modifier.size(46.dp),
                                            stroke = Stroke(3f),
                                            trackColor = trackColor,
                                            color = accentColor
                                        )
                                    }
                                }
                            }

                            else -> { // IDLE
                                IconButton(onClick = { onDownload() }) {
                                    Icon(
                                        Icons.Default.Download,
                                        contentDescription = stringResource(R.string.download),
                                        tint = accentColor
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                val progress =
                    if (state.duration > 0) (currentPosition.toFloat() / state.duration.toFloat()).coerceIn(
                        0f, 1f
                    ) else 0f
                var isDragging by remember(song.id) { mutableStateOf(false) }
                var dragProgress by remember(song.id) { mutableFloatStateOf(progress) }

                val displayProgress = if (isDragging) dragProgress else progress

                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.bufferState) {
                            LinearWavyProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp),
                                color = accentColor,
                                trackColor = trackColor
                            )
                        } else LinearWavyProgressIndicator(
                            progress = { displayProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .pointerInput(song.id, state.duration) {
                                    detectTapGestures { offset ->
                                        val seekFraction = (offset.x / size.width).coerceIn(0f, 1f)
                                        onSeek((seekFraction * state.duration).toLong())
                                    }
                                }
                                .pointerInput(song.id, state.duration) {
                                    detectHorizontalDragGestures(
                                        onDragStart = {
                                            isDragging = true
                                        },
                                        onDragEnd = {
                                            isDragging =
                                                false; onSeek((dragProgress * state.duration).toLong())
                                        },
                                        onDragCancel = { isDragging = false },
                                        onHorizontalDrag = { change, _ ->
                                            change.consume()
                                            dragProgress =
                                                (change.position.x / size.width).coerceIn(0f, 1f)
                                        })
                                },
                            color = accentColor,
                            waveSpeed = 20.dp,
                            trackColor = trackColor,
                            amplitude = { if (state.isPlaying && !isDragging && !state.bufferState) 1f else 0f })

                        if (!state.bufferState) Canvas(modifier = Modifier.fillMaxSize()) {
                            val thumbX = displayProgress * size.width
                            val pillWidth = 6.dp.toPx()   // Thinner width
                            val pillHeight = 20.dp.toPx() // Vertical height

                            drawRoundRect(
                                color = thumbColor,
                                topLeft = Offset(
                                    x = thumbX - (pillWidth / 2f),
                                    y = (size.height / 2f) - (pillHeight / 2f)
                                ),
                                size = Size(pillWidth, pillHeight),
                                cornerRadius = CornerRadius(pillWidth / 2f) // Makes it perfectly rounded (a pill)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            formatTime(currentPosition),
                            color = Color.White.copy(0.6f),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            if (state.bufferState) stringResource(R.string.loading) else formatTime(
                                state.duration
                            ),
                            color = Color.White.copy(0.6f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                Spacer(Modifier.weight(0.1f))

                // CONTROLS (Snappier LED colors & Integrated Buffering Spinner)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MetallicTrackButton(
                        icon = Icons.Rounded.SkipPrevious,
                        onClick = { onQueueStep(-1) },
                        contentDescription = stringResource(R.string.queue_previous)
                    )

                    IconButton(onClick = { onSeek(currentPosition - 10_000) }) {
                        Icon(
                            Icons.Rounded.Replay10,
                            stringResource(R.string.rewind_10_seconds),
                            tint = Color.White
                        )
                    }

                    Box(contentAlignment = Alignment.Center) {
                        MetallicPlayPauseButton(
                            isPlaying = state.isPlaying,
                            isLoading = state.bufferState,
                            onClick = onTogglePlayPause,
                            isAnimActive = isAnimActive,
                            accentColor = accentColor
                        )
                    }

                    IconButton(onClick = { onSeek(currentPosition + 10_000) }) {
                        Icon(
                            Icons.Rounded.Forward10,
                            stringResource(R.string.forward_10_seconds),
                            tint = Color.White
                        )
                    }

                    MetallicTrackButton(
                        icon = Icons.Rounded.SkipNext,
                        onClick = { onQueueStep(1) },
                        contentDescription = stringResource(R.string.queue_next)
                    )
                }
                if (state.queue != null) {
                    IconButton(onClick = onShowQueue, modifier = Modifier.size(40.dp)) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            stringResource(R.string.queue_show),
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
    if (showSongDetails) {
        SongDetailsSheet(
            song = song,
            duration = state.duration,
            downloadEntity = downloadEntity,
            accentColor = accentColor,
            onDismiss = { showSongDetails = false })
    }
}

@Composable
fun MetallicPlayPauseButton(
    isPlaying: Boolean,
    isAnimActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    val pulse = remember { Animatable(1f) }
    LaunchedEffect(isLoading, isAnimActive) {
        if (isLoading && isAnimActive) {
            pulse.animateTo(0.25f, infiniteRepeatable(tween(650), RepeatMode.Reverse))
        } else pulse.snapTo(1f)
    }
    val ledColor by animateColorAsState(
        when {
            isLoading -> Color(0xFF3399FF); isPlaying -> Color(0xFF00FF88); else -> Color(0xFFFF3344)
        }, tween(150), label = "playback status"
    )
    val buttonColor = lerp(accentColor, Color.White, 0.08f)
    val iconColor = lerp(accentColor, Color.Black, 0.86f)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledIconButton(
            onClick = onClick,
            enabled = !isLoading,
            shape = RoundedCornerShape(22.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color.Transparent,
                contentColor = iconColor,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = iconColor.copy(alpha = 0.65f)
            ),
            modifier = modifier
                .size(88.dp, 72.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            lerp(buttonColor, Color.White, 0.15f),
                            buttonColor.copy(alpha = if (isLoading) 0.7f else 0.96f)
                        )
                    ), RoundedCornerShape(22.dp)
                )
                .border(1.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
        ) {
            AnimatedContent(targetState = isPlaying, label = "play pause") { playing ->
                Icon(
                    if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (playing) stringResource(R.string.pause) else stringResource(
                        R.string.play
                    ),
                    modifier = Modifier.size(38.dp)
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Box(
            Modifier
                .size(6.dp, 4.dp)
                .graphicsLayer { alpha = pulse.value }
                .shadow(12.dp, CircleShape, spotColor = ledColor, ambientColor = ledColor)
                .background(ledColor, RoundedCornerShape(2.dp)))
    }
}

@Composable
private fun MetallicTrackButton(
    icon: ImageVector, onClick: () -> Unit, contentDescription: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val direction = if (icon == Icons.Rounded.SkipNext) 1f else -1f
    val arrowAngle by animateFloatAsState(
        targetValue = if (pressed) direction * 16f else 0f, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium
        ), label = "seek arrow press"
    )
    FilledIconButton(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(16.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = Color.White.copy(alpha = 0.12f), contentColor = Color.White
        ),
        modifier = Modifier
            .size(52.dp)
            .border(
                1.dp, Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.4f), Color.White.copy(alpha = 0.06f)
                    )
                ), RoundedCornerShape(16.dp)
            )
    ) {
        Icon(
            icon,
            contentDescription,
            modifier = Modifier
                .size(28.dp)
                .graphicsLayer { rotationZ = arrowAngle },
            tint = Color.White.copy(alpha = 0.85f)
        )
    }
}

fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(java.util.Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
