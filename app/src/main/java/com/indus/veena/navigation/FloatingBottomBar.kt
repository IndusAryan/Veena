package com.indus.veena.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.indus.veena.R
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

@Composable
fun FloatingBottomBar(navController: NavHostController, hazeState: HazeState) {
    val items = remember {
        listOf(
            BottomNavItem("Home", R.drawable.ic_home, Screen.Home, Color(0xFF56CCF2)),
          //  BottomNavItem("Trending", Icons.Default.Whatshot, Screen.Trending, Color(0xFFFF5F6D)),
            BottomNavItem("Downloads", R.drawable.ic_downloads, Screen.Downloads, Color(0xFFBB86FC)),
            BottomNavItem("Settings", R.drawable.ic_setting, Screen.Settings, Color(0xFF6FCF97))
        )
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val selectedIndex = items.indexOfFirst { item ->
        navBackStackEntry?.destination?.hierarchy?.any { it.hasRoute(item.destination::class) } == true
    }.coerceAtLeast(0)

    val animatedIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioLowBouncy),
        label = "index"
    )

    val animatedColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.primary,
        animationSpec = tween(500),
        label = "color"
    )

    Box(
        modifier = Modifier
            .padding(bottom = 12.dp)
            .fillMaxWidth(0.6f)
            .height(64.dp)
            .shadow(
                elevation = 20.dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = 0.6f),
                spotColor = animatedColor.copy(alpha = 0.3f)
            )
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    tint = HazeTint(Color.Black.copy(alpha = 0.3f)),
                    blurRadius = 20.dp
                )
            )
            .border(
                width = 0.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(0.4f), Color.White.copy(0.1f))
                ),
                shape = CircleShape
            )
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .blur(40.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
        ) {
            val tabWidth = size.width / items.size
            drawCircle(
                color = animatedColor.copy(alpha = 0.45f),
                radius = size.height / 1.5f,
                center = Offset(
                    x = (tabWidth * animatedIndex) + tabWidth / 2,
                    y = size.height / 2
                )
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
        ) {
            val tabWidth = size.width / items.size
            val path = androidx.compose.ui.graphics.Path().apply {
                addRoundRect(RoundRect(size.toRect(), CornerRadius(size.height)))
            }
            val activeTabCenterX = (tabWidth * animatedIndex) + (tabWidth / 2)
            drawPath(
                path = path,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        animatedColor.copy(alpha = 0f),
                        animatedColor.copy(alpha = 0.8f),
                        animatedColor.copy(alpha = 0.8f),
                        animatedColor.copy(alpha = 0f),
                    ),
                    startX = activeTabCenterX - (tabWidth * 0.7f),
                    endX = activeTabCenterX + (tabWidth * 0.7f),
                ),
                style = Stroke(width = 4f)
            )
        }

        Row(modifier = Modifier.fillMaxSize()) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex

                val alpha by animateFloatAsState(if (isSelected) 1f else 0.5f, label = "a")
                val scale by animateFloatAsState(if (isSelected) 1.15f else 1f, label = "s")

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            navController.navigate(item.destination) {
                                popUpTo<Screen.Home> {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(item.icon),
                            contentDescription = item.name,
                            tint = if (isSelected) Color.White else Color.White.copy(0.7f)
                        )
                    }
                }
            }
        }
    }
}
