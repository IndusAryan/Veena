package com.indus.veena.extension

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.indus.veena.database.DataStoreKeys
import com.indus.veena.repository.MusicRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddonAutoUpdater @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val store: ExtensionStoreManager,
    private val repository: MusicRepository
) {
    fun start(scope: CoroutineScope) = scope.launch {
        if (dataStore.data.first()[DataStoreKeys.AUTO_UPDATE_ADDONS] != true) return@launch
        repository.awaitExtensionsLoaded()
        try { store.updateInstalledAddons() }
        catch (e: CancellationException) { throw e }
        catch (e: Exception) { android.util.Log.w("AddonAutoUpdater", "Startup update check failed", e) }
    }
}
