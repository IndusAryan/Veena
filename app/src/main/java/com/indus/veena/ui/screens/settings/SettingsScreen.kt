package com.indus.veena.ui.screens.settings

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indus.veena.database.DataStoreKeys
import com.indus.veena.ui.theme.VeenaAccent


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onDebugMenuClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val currentSuggestionId by viewModel.currentSuggestionProvider.collectAsStateWithLifecycle()
    val availableSuggestionProviders by viewModel.availableSuggestionProviders.collectAsStateWithLifecycle()
    val selectedProviderName = remember(currentSuggestionId, availableSuggestionProviders) {
        availableSuggestionProviders.find { it.id == currentSuggestionId }?.name ?: "iTunes"
    }
    val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
    val currentQuality by viewModel.currentQuality.collectAsStateWithLifecycle()
    val currentAccent by viewModel.currentAccent.collectAsStateWithLifecycle()

    var showSuggestionDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var showDownloadQualityDialog by remember { mutableStateOf(false) }
    val currentDownloadQuality by viewModel.currentDownloadQuality.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold
                    )
                },
                //windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .verticalScroll(rememberScrollState())
                .padding(bottom = 120.dp) // room for bottom nav
        ) {
            SettingsSectionHeader(icon = Icons.Outlined.Palette, title = "Appearance")

            SettingsCard {
                SettingsRow(
                    icon = Icons.Outlined.DarkMode,
                    title = "App Theme",
                    subtitle = currentTheme.name.lowercase().replaceFirstChar { it.uppercase() },
                    onClick = { showThemeDialog = true }
                )
            }

            Spacer(Modifier.height(12.dp))

            // Accent color picker — card style
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                tonalElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(vertical = 16.dp)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Accent Color",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(VeenaAccent.entries) { accent ->
                            AccentSwatch(
                                accent = accent,
                                isSelected = currentAccent == accent,
                                onClick = { viewModel.setAccent(accent) }
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Current: ${currentAccent.displayName}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        textAlign = TextAlign.Start
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            SettingsSectionHeader(icon = Icons.Outlined.HighQuality, title = "Audio & Playback")

            SettingsCard {
                SettingsRow(
                    icon = Icons.Outlined.HighQuality,
                    title = "Streaming Quality",
                    subtitle = when (currentQuality) {
                        DataStoreKeys.AudioQuality.HIGH -> "High · 320 kbps"
                        DataStoreKeys.AudioQuality.MEDIUM -> "Standard · 160 kbps"
                        DataStoreKeys.AudioQuality.LOW -> "Data Saver · 48 kbps"
                    },
                    onClick = { showQualityDialog = true }
                )
                SettingsRow(
                    icon = Icons.Outlined.Download,
                    title = "Download Quality",
                    subtitle = when (currentDownloadQuality) {
                        DataStoreKeys.AudioQuality.HIGH -> "High · 320 kbps"
                        DataStoreKeys.AudioQuality.MEDIUM -> "Standard · 160 kbps"
                        DataStoreKeys.AudioQuality.LOW -> "Data Saver · 48 kbps"
                    },
                    onClick = { showDownloadQualityDialog = true }
                )
            }

            Spacer(Modifier.height(24.dp))

            SettingsSectionHeader(icon = Icons.Outlined.Search, title = "General")

            SettingsCard {
                SettingsRow(
                    icon = Icons.Outlined.Search,
                    title = "Search Suggestions",
                    subtitle = "Source: $selectedProviderName",
                    onClick = { showSuggestionDialog = true }
                )
            }
        }
    }

    if (showThemeDialog) {
        SettingSelectionDialog(
            title = "App Theme",
            options = DataStoreKeys.AppTheme.entries,
            currentSelection = currentTheme,
            onDismiss = { showThemeDialog = false },
            onSelect = { viewModel.setTheme(it) },
            labelProvider = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
        )
    }

    if (showQualityDialog) {
        SettingSelectionDialog(
            title = "Streaming Quality",
            options = DataStoreKeys.AudioQuality.entries,
            currentSelection = currentQuality,
            onDismiss = { showQualityDialog = false },
            onSelect = { viewModel.setAudioQuality(it) },
            labelProvider = {
                when (it) {
                    DataStoreKeys.AudioQuality.HIGH   -> "High (Best Audio)"
                    DataStoreKeys.AudioQuality.MEDIUM -> "Standard (Balanced)"
                    DataStoreKeys.AudioQuality.LOW    -> "Low (Save Data)"
                }
            }
        )
    }

    if (showSuggestionDialog) {
        SettingSelectionDialog(
            title = "Suggestion Source",
            options = availableSuggestionProviders,
            currentSelection = availableSuggestionProviders.find { it.id == currentSuggestionId },
            onDismiss = { showSuggestionDialog = false },
            onSelect = { it?.id?.let { providerId -> viewModel.setSuggestionProvider(providerId) } },
            labelProvider = { it?.name ?: "" }
        )
    }
    if (showDownloadQualityDialog) {
        SettingSelectionDialog(
            title = "Download Quality",
            options = DataStoreKeys.AudioQuality.entries,
            currentSelection = currentDownloadQuality,
            onDismiss = { showDownloadQualityDialog = false },
            onSelect = { viewModel.setDownloadQuality(it) },
            labelProvider = {
                when (it) {
                    DataStoreKeys.AudioQuality.HIGH   -> "High (Best Audio)"
                    DataStoreKeys.AudioQuality.MEDIUM -> "Standard (Balanced)"
                    DataStoreKeys.AudioQuality.LOW    -> "Low (Save Storage)"
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(icon: ImageVector, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 1.dp
    ) {
        Column { content() }
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
             //   indication = rememberRipple()
            ) { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon pill
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            modifier = Modifier.size(42.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun AccentSwatch(
    accent: VeenaAccent,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val ringSize by animateDpAsState(
        targetValue = if (isSelected) 56.dp else 52.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "ring_size"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(60.dp)
    ) {
        Box(
            modifier = Modifier
                .size(ringSize)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                  //  indication = rememberRipple(bounded = false, radius = 30.dp)
                ) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            // Outer ring when selected
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                )
            }

            // Color circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (accent == VeenaAccent.MATERIAL_YOU) {
                            Brush.sweepGradient(
                                listOf(
                                    Color(0xFF00BCD4),
                                    Color(0xFFE91E63),
                                    Color(0xFFFFEB3B),
                                    Color(0xFF4CAF50),
                                    Color(0xFF00BCD4)
                                )
                            )
                        } else {
                            SolidColor(accent.color)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = accent.displayName.split(" ").first(),
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun <T> SettingSelectionDialog(
    title: String,
    options: List<T>,
    currentSelection: T,
    onDismiss: () -> Unit,
    onSelect: (T) -> Unit,
    labelProvider: (T) -> String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column {
                options.forEach { option ->
                    val isSelected = option == currentSelection
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        else Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(option)
                                    onDismiss()
                                }
                                .padding(horizontal = 8.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = null
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = labelProvider(option),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss", fontWeight = FontWeight.Medium)
            }
        }
    )
}