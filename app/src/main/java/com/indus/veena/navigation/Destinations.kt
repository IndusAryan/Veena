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
    @Serializable data object Settings : Screen
    @Serializable data object Player : Screen
    @Serializable data object Downloads: Screen
    @Serializable data object DebugScreen: Screen
}

@Stable
data class BottomNavItem<T : Any>(
    val name: String,
    val icon: Int,
    val destination: T,
    val color: Color
)