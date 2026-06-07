package com.indus.veena.navigation

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
import com.indus.veena.models.SongModel
import com.indus.veena.ui.screens.addons.AddonsScreen
import com.indus.veena.ui.screens.debugmenu.DebugBackupScreen
import com.indus.veena.ui.screens.downloads.DownloadsScreen
import com.indus.veena.ui.screens.home.HomeScreen
import com.indus.veena.ui.screens.player.PlayerViewModel
import com.indus.veena.ui.screens.settings.SettingsScreen
import com.indus.veena.ui.screens.splash.VolumetricSplashScreen
import com.indus.veena.util.popCurrentPage

@Composable
fun NavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues,
    playerState: PlayerViewModel.PlayerState,
    dominantColor: Color?,
    onSongSelected: (SongModel) -> Unit
) {
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
                onSongClick = { song -> onSongSelected(song) }
            )
        }
        composable<Screen.Trending> { TrendingScreen() }
        composable<Screen.Settings> { SettingsScreen(
            onDebugMenuClick = { navController.navigate(Screen.DebugScreen) },
            onAddonsClick = { navController.navigate(Screen.Addons) }
        )
        }
        composable<Screen.Addons> { AddonsScreen(navController::popCurrentPage) }
        composable<Screen.Downloads> {
            DownloadsScreen(
                paddingValues = paddingValues,
                onSongClick = { song -> onSongSelected(song) },
                onBackClick = navController::popCurrentPage)
        }
        composable<Screen.DebugScreen> { DebugBackupScreen() }
    }
}

// Dummy screens for now
@Composable
fun TrendingScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Trending") }
}