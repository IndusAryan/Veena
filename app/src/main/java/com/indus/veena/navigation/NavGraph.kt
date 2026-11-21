package com.indus.veena.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.indus.veena.models.PlaybackQueue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.indus.veena.ui.screens.addons.AddonsScreen
import com.indus.veena.ui.screens.debugmenu.DebugBackupScreen
import com.indus.veena.ui.screens.downloads.DownloadsScreen
import com.indus.veena.ui.screens.favourites.FavouritesScreen
import com.indus.veena.ui.screens.home.HomeScreen
import com.indus.veena.ui.screens.player.PlayerState
import com.indus.veena.ui.screens.settings.SettingsScreen
import com.indus.veena.ui.screens.splash.VolumetricSplashScreen
import com.indus.veena.util.popCurrentPage

@Composable
fun NavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues,
    playerState: PlayerState,
    dominantColor: Color?,
    onSongSelected: (PlaybackQueue) -> Unit
) {
    val discoveryQueue: DiscoveryQueueViewModel = viewModel()
    fun openDiscovery(queue: PlaybackQueue) {
        discoveryQueue.queue = queue
        val song = queue.songs[queue.index]
        navController.navigate(Screen.DiscoverySong(
            kotlinx.serialization.json.Json.encodeToString(song.copy(streamableUrls = emptyMap()))
        ))
    }

    NavHost(
        navController = navController,
        startDestination = Screen.SplashScreen
    ) {
        composable<Screen.SplashScreen> {
            VolumetricSplashScreen {
                navController.navigate(Screen.Home) {
                    popUpTo<Screen.SplashScreen> { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
        composable<Screen.Home> {
            HomeScreen(
                paddingValues = paddingValues,
                playerDominantColor = dominantColor,
                playerState = playerState,
                onSongClick = onSongSelected,
                onAddonsClick = { navController.navigate(Screen.Addons) }
            )
        }
        composable<Screen.Trending>(
            enterTransition = { slideInHorizontally(tween(280)) { it } + fadeIn(tween(200)) },
            exitTransition = { slideOutHorizontally(tween(280)) { -it / 3 } + fadeOut(tween(200)) },
            popEnterTransition = { slideInHorizontally(tween(280)) { -it / 3 } + fadeIn(tween(200)) },
            popExitTransition = { slideOutHorizontally(tween(280)) { it } + fadeOut(tween(200)) }
        ) {
            com.indus.veena.ui.screens.trending.TrendingScreen(paddingValues,
                onSongClick = ::openDiscovery,
                onAddonsClick = { navController.navigate(Screen.Addons) }, accentColor = dominantColor,
                onCollectionClick = { item -> navController.navigate(Screen.Collection(item.provider, item.id,
                    item.kind, item.title, item.thumbnail)) })
        }
        composable<Screen.DiscoverySong>(
            enterTransition = { slideInHorizontally(tween(280)) { it } + fadeIn(tween(200)) },
            exitTransition = { slideOutHorizontally(tween(280)) { -it / 3 } + fadeOut(tween(200)) },
            popEnterTransition = { slideInHorizontally(tween(280)) { -it / 3 } + fadeIn(tween(200)) },
            popExitTransition = { slideOutHorizontally(tween(280)) { it } + fadeOut(tween(200)) }
        ) {
            com.indus.veena.ui.screens.trending.DiscoverySongScreen(paddingValues, playerState,
                onBack = navController::popCurrentPage, originQueue = discoveryQueue.queue, onPlay = onSongSelected)
        }
        composable<Screen.Collection>(
            enterTransition = { slideInHorizontally(tween(280)) { it } + fadeIn(tween(200)) },
            exitTransition = { slideOutHorizontally(tween(280)) { -it / 3 } + fadeOut(tween(200)) },
            popEnterTransition = { slideInHorizontally(tween(280)) { -it / 3 } + fadeIn(tween(200)) },
            popExitTransition = { slideOutHorizontally(tween(280)) { it } + fadeOut(tween(200)) }
        ) {
            com.indus.veena.ui.screens.trending.CollectionScreen(paddingValues,
                onBack = navController::popCurrentPage,
                onSongClick = ::openDiscovery)
        }
        composable<Screen.Settings> { SettingsScreen(
            onDebugMenuClick = { navController.navigate(Screen.DebugScreen) },
            onAddonsClick = { navController.navigate(Screen.Addons) }
        )
        }
        composable<Screen.Addons> { AddonsScreen(navController::popCurrentPage) }
        composable<Screen.Downloads> {
            DownloadsScreen(
                paddingValues = paddingValues,
                onSongClick = onSongSelected,
                onBackClick = navController::popCurrentPage,
                onShowAllFavourites = { navController.navigate(Screen.Favourites) }
            )
        }
        composable<Screen.Favourites> {
            FavouritesScreen(
                paddingValues = paddingValues,
                onBackClick = navController::popCurrentPage,
                onSongClick = onSongSelected
            )
        }
        composable<Screen.DebugScreen> { DebugBackupScreen() }
    }
}

class DiscoveryQueueViewModel : ViewModel() {
    var queue: PlaybackQueue? = null
}
