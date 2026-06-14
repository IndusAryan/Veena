package com.indus.veena

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.request.crossfade
import com.indus.veena.database.DataStoreKeys
import com.indus.veena.helpers.ImageModuleCoil
import com.indus.veena.navigation.FloatingBottomBar
import com.indus.veena.navigation.NavGraph
import com.indus.veena.navigation.Screen
import com.indus.veena.ui.screens.player.FullPlayerOverlay
import com.indus.veena.ui.screens.player.MiniPlayer
import com.indus.veena.ui.screens.player.PlayerDisplayMode
import com.indus.veena.ui.screens.player.PlayerViewModel
import com.indus.veena.ui.screens.settings.SettingsViewModel
import com.indus.veena.ui.theme.VeenaTheme
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    val settingsViewModel: SettingsViewModel by viewModels()
    val playerViewModel: PlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            val currentTheme by settingsViewModel.currentTheme.collectAsStateWithLifecycle(initialValue = initialSettings.theme)
            val currentAccent by settingsViewModel.currentAccent.collectAsStateWithLifecycle(initialValue = initialSettings.accent)
            val currentPosition by playerViewModel.currentPosition.collectAsStateWithLifecycle()

            val useDarkTheme = when (currentTheme) {
                DataStoreKeys.AppTheme.SYSTEM -> isSystemInDarkTheme()
                DataStoreKeys.AppTheme.LIGHT -> false
                DataStoreKeys.AppTheme.DARK -> true
            }

            VeenaTheme(
                darkTheme = useDarkTheme,
                accent = currentAccent
            ) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val playerState by playerViewModel.uiState.collectAsStateWithLifecycle()
                val displayMode = playerState.displayMode
                val hazeState = remember { HazeState() }

                val shouldShowBottomBar = currentDestination?.hierarchy?.any { dest ->
                    dest.hasRoute(Screen.Home::class) || dest.hasRoute(Screen.Trending::class) ||
                    dest.hasRoute(Screen.Settings::class) || dest.hasRoute(Screen.Downloads::class)
                } == true

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
                            onSongSelected = { song ->
                                playerViewModel.playSong(song) { toastMsg ->
                                    Toast.makeText(
                                        applicationContext,
                                        toastMsg,
                                        LENGTH_SHORT
                                    ).show()
                                }
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
                    }
                }
            }
            SingletonImageLoader.setSafe { ImageModuleCoil.buildImageLoader(applicationContext) }
        }
    }
}
