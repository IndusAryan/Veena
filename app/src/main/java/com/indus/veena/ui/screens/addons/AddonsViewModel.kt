package com.indus.veena.ui.screens.addons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indus.veena.extension.CatalogExtensionItem
import com.indus.veena.extension.ExtensionManager
import com.indus.veena.extension.ExtensionStoreManager
import com.indus.veena.models.UpdateInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LocalAddonItem(
    val id: String,
    val name: String,
    val version: String,
    val versionCode: Int,
    val author: String,
    val description: String,
    val capabilities: List<String>,
    val updateUrl: String
)

@HiltViewModel
class AddonsViewModel @Inject constructor(
    private val storeManager: ExtensionStoreManager,
    extensionManager: ExtensionManager
) : ViewModel() {

    private val _catalog = MutableStateFlow<List<CatalogExtensionItem>>(emptyList())
    val catalog = _catalog.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _uiEvent = Channel<String>()
    val uiEvent = _uiEvent.receiveAsFlow()

    val downloadProgress: StateFlow<Map<String, Float>> = storeManager.downloadProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _updateStatusMap = MutableStateFlow<Map<String, UpdateInfo>>(emptyMap())
    val updateStatusMap = _updateStatusMap.asStateFlow()

    val localInstalledAddons: StateFlow<List<LocalAddonItem>> = extensionManager.extensions.map { map ->
        map.values.map { ext ->
            LocalAddonItem(
                id = ext.manifest.id,
                name = ext.manifest.name,
                version = ext.manifest.version,
                versionCode = ext.manifest.versionCode,
                author = ext.manifest.author,
                description = ext.manifest.description,
                capabilities = ext.manifest.capabilities,
                updateUrl = ext.manifest.updateUrl
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())



    init {
        // Auto-check for updates on launch
        viewModelScope.launch {
            combine(localInstalledAddons, catalog) { installed, remoteCatalog ->
                Pair(installed, remoteCatalog)
            }.collect { (installed, remoteCatalog) ->
                checkUpdates(installed, remoteCatalog)
            }
        }
        refreshCatalog()
    }

    fun refreshCatalog() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                _catalog.value = storeManager.fetchCatalog()
                checkUpdates(localInstalledAddons.value, _catalog.value)
            } catch (t: Throwable) {
                _uiEvent.send("Failed to refresh catalog: ${t.localizedMessage}")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private suspend fun checkUpdates(installed: List<LocalAddonItem>, remoteCatalog: List<CatalogExtensionItem>) {
        installed.forEach { addon ->
            val catalogItem = remoteCatalog.find { it.id == addon.id }
            val downloadUrl = catalogItem?.downloadUrl ?: ""

            if (downloadUrl.contains("github.com")) {
                val info = storeManager.fetchGithubUpdateInfo(downloadUrl, addon.id)
                if (info != null && compareVersions(info.version, addon.version)) {
                    _updateStatusMap.update { it + (addon.id to info) }
                }
            }
        }
    }

    private fun compareVersions(remote: String, local: String): Boolean {
        val rParts = remote.removePrefix("v").split(".").mapNotNull { it.toIntOrNull() }
        val lParts = local.removePrefix("v").split(".").mapNotNull { it.toIntOrNull() }
        val size = maxOf(rParts.size, lParts.size)
        for (i in 0 until size) {
            val r = rParts.getOrElse(i) { 0 }
            val l = lParts.getOrElse(i) { 0 }
            if (r > l) return true
            if (l > r) return false
        }
        return false
    }

    fun installAddon(id: String, downloadUrl: String, name: String = id) {
        viewModelScope.launch {
            val mockItem = CatalogExtensionItem(
                id = id,
                name = name,
                version = "",
                versionCode = 0,
                downloadUrl = downloadUrl,
                iconUrl = "",
                description = "",
                author = "",
                isOfficial = false,
                size = "",
                capabilities = emptyList()
            )
            try {
                storeManager.installExtension(mockItem)
                _updateStatusMap.update { it - id }
                _uiEvent.send("Successfully installed $name")
            } catch (t: Throwable) {
                _uiEvent.send("Failed to install $name: ${t.localizedMessage}")
                refreshCatalog()
            }
        }
    }

    fun deleteAddon(id: String, name: String = id) {
        viewModelScope.launch {
            try {
                storeManager.deleteExtension(id)
                _updateStatusMap.update { it - id }
                _uiEvent.send("Uninstalled $name")
            } catch (t: Throwable) {
                _uiEvent.send("Failed to delete $name")
                refreshCatalog()
            }
        }
    }
}