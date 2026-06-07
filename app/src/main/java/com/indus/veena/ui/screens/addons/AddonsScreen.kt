package com.indus.veena.ui.screens.addons

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.indus.veena.contract.LoadedExtension
import com.indus.veena.extension.CatalogExtensionItem


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddonsScreen(
    onBackClick: () -> Unit,
    viewModel: AddonsViewModel = hiltViewModel()
) {
    val catalog by viewModel.catalog.collectAsStateWithLifecycle()
    val installed by viewModel.installedExtensions.collectAsStateWithLifecycle()
    val downloadProgress by viewModel.downloadProgress.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Installed", "Official", "Community")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Addon Manager", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshCatalog() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Catalog")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                this@Column.AnimatedVisibility(
                    visible = isRefreshing,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .zIndex(1f)
                ) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                when (selectedTab) {
                    0 -> {
                        val installedList = catalog.filter { installed.containsKey(it.id) }
                        if (installedList.isEmpty()) {
                            EmptyStatePlaceholder("No addons installed locally.")
                        } else {
                            AddonList(installedList, installed, downloadProgress, viewModel::installAddon, viewModel::deleteAddon)
                        }
                    }
                    1 -> {
                        val officialList = catalog.filter { it.isOfficial }
                        AddonList(officialList, installed, downloadProgress, viewModel::installAddon, viewModel::deleteAddon)
                    }
                    2 -> {
                        val communityList = catalog.filter { !it.isOfficial }
                        AddonList(communityList, installed, downloadProgress, viewModel::installAddon, viewModel::deleteAddon)
                    }
                }
            }
        }
    }
}

@Composable
fun AddonList(
    items: List<CatalogExtensionItem>,
    installed: Map<String, LoadedExtension>,
    downloadProgress: Map<String, Float>,
    onInstall: (CatalogExtensionItem) -> Unit,
    onDelete: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items, key = { it.id }) { item ->
            val localExtension = installed[item.id]
            val isUpdateAvailable = localExtension != null && item.versionCode > localExtension.manifest.versionCode

            AddonCard(
                item = item,
                isInstalled = localExtension != null,
                isUpdateAvailable = isUpdateAvailable,
                downloadProgress = downloadProgress[item.id],
                onInstall = { onInstall(item) },
                onDelete = { onDelete(item.id) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddonCard(
    item: CatalogExtensionItem,
    isInstalled: Boolean,
    isUpdateAvailable: Boolean,
    downloadProgress: Float?,
    onInstall: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = item.iconUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        error = rememberVectorPainter(Icons.Default.Extension)
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (item.isOfficial) {
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(14.dp)
                            ) {}
                        }
                    }
                    Text(
                        text = "v${item.version} • ${item.author} • ${item.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.width(8.dp))

                Box(contentAlignment = Alignment.Center) {
                    if (downloadProgress != null) {
                        CircularProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 3.dp
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isUpdateAvailable) {
                                Button(
                                    onClick = onInstall,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(32.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SystemUpdate,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Update", style = MaterialTheme.typography.labelMedium)
                                }
                            } else if (!isInstalled) {
                                Button(
                                    onClick = onInstall,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Install", style = MaterialTheme.typography.labelMedium)
                                }
                            }

                            if (isInstalled) {
                                Spacer(Modifier.width(4.dp))
                                IconButton(onClick = onDelete) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Uninstall Addon",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item.capabilities.forEach { capability ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(0.dp)
                    ) {
                        Text(
                            text = capability,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStatePlaceholder(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}