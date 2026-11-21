package com.indus.veena.navigation

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

@Immutable
sealed interface Screen {
    @Serializable data object SplashScreen: Screen
    @Serializable data object Home : Screen
    @Serializable data object Trending : Screen
    @Serializable data class DiscoverySong(val song: String) : Screen
    @Serializable data class Collection(val provider: String, val id: String, val kind: String,
        val title: String, val thumbnail: String = "") : Screen
    @Serializable data object Settings : Screen
    @Serializable data object Addons : Screen
    @Serializable data object Player : Screen
    @Serializable data object Downloads: Screen
    @Serializable data object Favourites: Screen
    @Serializable data object DebugScreen: Screen
}

@Stable
data class BottomNavItem<T : Any>(
    @androidx.annotation.StringRes val name: Int,
    val icon: Int,
    val destination: T,
    val color: Color
)