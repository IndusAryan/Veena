package com.indus.veena.ui.screens.addons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indus.veena.contract.LoadedExtension
import com.indus.veena.extension.CatalogExtensionItem
import com.indus.veena.extension.ExtensionManager
import com.indus.veena.extension.ExtensionStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddonsViewModel @Inject constructor(
    private val storeManager: ExtensionStoreManager,
    private val extensionManager: ExtensionManager
) : ViewModel() {

    private val _catalog = MutableStateFlow<List<CatalogExtensionItem>>(emptyList())
    val catalog = _catalog.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    val installedExtensions: StateFlow<Map<String, LoadedExtension>> = extensionManager.extensions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val downloadProgress: StateFlow<Map<String, Float>> = storeManager.downloadProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    init {
        refreshCatalog()
    }

    fun refreshCatalog() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                _catalog.value = storeManager.fetchCatalog()
            } catch (e: Exception) {
                // Handled gracefully inside storeManager (asset fallback)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun installAddon(item: CatalogExtensionItem) {
        viewModelScope.launch {
            try {
                storeManager.installExtension(item)
            } catch (e: Exception) {
                _catalog.value = storeManager.fetchCatalog() // Force refresh if error
            }
        }
    }

    fun deleteAddon(id: String) {
        viewModelScope.launch {
            try {
                storeManager.deleteExtension(id)
            } catch (e: Exception) {
                // Logged in store manager
            }
        }
    }
}