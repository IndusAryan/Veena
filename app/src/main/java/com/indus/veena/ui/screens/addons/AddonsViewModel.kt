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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.CancellationException
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
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    private val storeManager: ExtensionStoreManager,
    extensionManager: ExtensionManager
) : ViewModel() {

    private val _catalog = MutableStateFlow<List<CatalogExtensionItem>>(emptyList())
    val catalog = _catalog.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _uiEvent = Channel<String>(Channel.BUFFERED)
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
            }.collectLatest { (installed, remoteCatalog) ->
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
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                _uiEvent.send(context.getString(com.indus.veena.R.string.catalog_refresh_failed, t.localizedMessage.orEmpty()))
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private fun checkUpdates(installed: List<LocalAddonItem>, remoteCatalog: List<CatalogExtensionItem>) {
        _updateStatusMap.update { status -> status.filterKeys { id -> installed.any { it.id == id } } }
        installed.forEach { addon ->
            val catalogItem = remoteCatalog.find { it.id == addon.id }
            if (catalogItem != null && catalogItem.versionCode > addon.versionCode) {
                val info = UpdateInfo(
                    version = catalogItem.version,
                    downloadUrl = catalogItem.downloadUrl,
                    size = catalogItem.size,
                    versionCode = catalogItem.versionCode
                )
                _updateStatusMap.update { it + (addon.id to info) }
            } else {
                _updateStatusMap.update { it - addon.id }
            }
        }
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
                val updated = storeManager.installExtension(mockItem)
                _updateStatusMap.update { it - id }
                if (!updated) _uiEvent.send(context.getString(com.indus.veena.R.string.addon_installed, name))
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                _uiEvent.send(context.getString(com.indus.veena.R.string.addon_install_failed, name, t.localizedMessage.orEmpty()))
                refreshCatalog()
            }
        }
    }

    fun deleteAddon(id: String, name: String = id) {
        viewModelScope.launch {
            try {
                storeManager.deleteExtension(id)
                _updateStatusMap.update { it - id }
                _uiEvent.send(context.getString(com.indus.veena.R.string.addon_uninstalled, name))
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                _uiEvent.send(context.getString(com.indus.veena.R.string.addon_delete_failed, name))
                refreshCatalog()
            }
        }
    }
}