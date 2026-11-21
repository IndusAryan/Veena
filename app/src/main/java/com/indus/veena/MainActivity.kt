package com.indus.veena

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil3.SingletonImageLoader
import com.indus.veena.database.DataStoreKeys
import com.indus.veena.helpers.ImageModuleCoil
import com.indus.veena.navigation.FloatingBottomBar
import com.indus.veena.navigation.NavGraph
import com.indus.veena.navigation.Screen
import com.indus.veena.ui.screens.player.FullPlayerOverlay
import com.indus.veena.ui.screens.player.MiniPlayer
import com.indus.veena.ui.screens.player.PlayerDisplayMode
import com.indus.veena.ui.screens.player.PlaybackCoordinator
import com.indus.veena.ui.screens.settings.SettingsViewModel
import com.indus.veena.ui.theme.VeenaTheme
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @javax.inject.Inject lateinit var extensionStore: com.indus.veena.extension.ExtensionStoreManager

    val settingsViewModel: SettingsViewModel by viewModels()
    @javax.inject.Inject lateinit var playerViewModel: PlaybackCoordinator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (com.indus.veena.crash.CrashRecovery.hasPending(this)) {
            com.indus.veena.crash.CrashRecovery.open(this)
            finish()
            return
        }
        val initialSettings = runBlocking {
            settingsViewModel.getInitialSettings()
        }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.Transparent.toArgb(), Color.Transparent.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(Color.Transparent.toArgb())
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        setContent {
            SingletonImageLoader.setSafe { ImageModuleCoil.buildImageLoader(applicationContext) }

            val currentTheme by settingsViewModel.currentTheme.collectAsStateWithLifecycle(initialValue = initialSettings.theme)
            val currentAccent by settingsViewModel.currentAccent.collectAsStateWithLifecycle(initialValue = initialSettings.accent)
            val currentPosition by playerViewModel.currentPosition.collectAsStateWithLifecycle()
            val useDarkTheme = when (currentTheme) {
                DataStoreKeys.AppTheme.SYSTEM, DataStoreKeys.AppTheme.SYSTEM_AMOLED -> isSystemInDarkTheme()
                DataStoreKeys.AppTheme.LIGHT -> false
                DataStoreKeys.AppTheme.DARK -> true
                DataStoreKeys.AppTheme.AMOLED -> true
            }

            VeenaTheme(
                darkTheme = useDarkTheme,
                isAmoled = currentTheme == DataStoreKeys.AppTheme.AMOLED ||
                    currentTheme == DataStoreKeys.AppTheme.SYSTEM_AMOLED,
                accent = currentAccent
            ) {
                var hasNotificationPermission by remember {
                    mutableStateOf(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        } else true
                    )
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted ->
                        hasNotificationPermission = isGranted
                        if (!isGranted) {
                            Toast.makeText(this@MainActivity, this@MainActivity.getString(com.indus.veena.R.string.notification_permission_explanation), Toast.LENGTH_LONG).show()
                        }
                    }
                )


                val themePrimary = androidx.compose.material3.MaterialTheme.colorScheme.primary
                LaunchedEffect(themePrimary) {
                    val hex = String.format("#%08X", themePrimary.toArgb())
                    Log.d("VeenaTheme", "Active Theme Primary Color (Hex): $hex")
                }
                LaunchedEffect(Unit) {
                    playerViewModel.toastEvent.collectLatest { message ->
                        Toast.makeText(this@MainActivity, message, LENGTH_SHORT).show()
                    }
                }
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                var requestedNotifications by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
                LaunchedEffect(currentDestination) {
                    if (currentDestination?.hasRoute(Screen.Home::class) == true &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission && !requestedNotifications) {
                        requestedNotifications = true
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                val playerState by playerViewModel.uiState.collectAsStateWithLifecycle()
                val displayMode = playerState.displayMode
                val hazeState = remember { HazeState() }

                val shouldShowBottomBar = currentDestination?.hierarchy?.any { dest ->
                    dest.hasRoute(Screen.Home::class) || dest.hasRoute(Screen.Trending::class) ||
                    dest.hasRoute(Screen.Settings::class) || dest.hasRoute(Screen.Downloads::class)
                } == true

                val addonSnackbar = remember { androidx.compose.material3.SnackbarHostState() }
                LaunchedEffect(extensionStore) {
                    extensionStore.updateEvents.collect { addonSnackbar.showSnackbar(it) }
                }
                LaunchedEffect(playerViewModel) {
                    playerViewModel.queueEndEvent.collectLatest {
                        addonSnackbar.showSnackbar(getString(R.string.queue_end))
                    }
                }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        AnimatedVisibility(
                            visible = (displayMode == PlayerDisplayMode.MINI || shouldShowBottomBar) && displayMode != PlayerDisplayMode.FULL,
                            enter = slideInVertically { it } + fadeIn(),
                            exit = slideOutVertically { it } + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.3f),
                                                Color.Black.copy(alpha = 0.6f)
                                            )
                                        )
                                    )
                                    .navigationBarsPadding()
                                    .padding(bottom = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AnimatedVisibility(
                                    visible = displayMode == PlayerDisplayMode.MINI,
                                    exit = slideOutVertically { it } + fadeOut()
                                ) {
                                    Column {
                                        MiniPlayer(
                                            state = playerState,
                                            hazeState = hazeState,
                                            currentPosition = currentPosition,
                                            onTogglePlay = playerViewModel::togglePlayPause,
                                            onDismiss = playerViewModel::stopPlayer,
                                            onExpand = {
                                                playerViewModel.setDisplayMode(
                                                    PlayerDisplayMode.FULL
                                                )
                                            }
                                        )
                                        if (shouldShowBottomBar) {
                                            Spacer(Modifier.height(8.dp))
                                        }
                                    }
                                }
                                AnimatedVisibility(
                                    visible = shouldShowBottomBar,
                                    enter = slideInVertically { it } + fadeIn(),
                                    exit = slideOutVertically { it } + fadeOut()
                                ) {
                                    FloatingBottomBar(navController, hazeState)
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(state = hazeState)
                    ) {
                        NavGraph(
                            navController = navController,
                            paddingValues = innerPadding,
                            playerState = playerState,
                            dominantColor = if (playerState.isPlaying) playerState.dominantColor else currentAccent.color,
                            onSongSelected = { queue ->
                                playerViewModel.playQueue(queue)
                                playerViewModel.setDisplayMode(PlayerDisplayMode.FULL)
                            }
                        )
                        AnimatedVisibility(
                            visible = displayMode == PlayerDisplayMode.FULL,
                            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                        ) {
                            FullPlayerOverlay(
                                state = playerState,
                                viewModel = playerViewModel,
                                onCollapse = { playerViewModel.setDisplayMode(PlayerDisplayMode.MINI) }
                            )
                        }
                        androidx.compose.material3.SnackbarHost(
                            hostState = addonSnackbar,
                            modifier = Modifier.align(Alignment.BottomCenter).padding(
                                bottom = if (displayMode == PlayerDisplayMode.FULL) 0.dp
                                else innerPadding.calculateBottomPadding()
                            ).navigationBarsPadding()
                        )
                    }
                }
            }
        }
    }
}
