package com.indus.veena.ui.screens.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indus.veena.database.DataStoreKeys
import com.indus.veena.database.DataStoreKeys.SUGGESTION_PROVIDER_KEY
import com.indus.veena.di.DataStoreModule.getValueFlow
import com.indus.veena.helpers.safeValueOf
import com.indus.veena.helpers.saveEnum
import com.indus.veena.repository.MusicRepository
import com.indus.veena.ui.theme.VeenaAccent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    musicRepository: MusicRepository
    ): ViewModel() {

    suspend fun getInitialSettings(): InitialSettings {
        val prefs = dataStore.data.first()
        val theme = prefs[DataStoreKeys.APP_THEME_KEY] ?: DataStoreKeys.AppTheme.SYSTEM.name
        val accent = prefs[DataStoreKeys.APP_ACCENT_KEY] ?: VeenaAccent.MATERIAL_YOU.name
        return InitialSettings(
            theme = safeValueOf(theme, DataStoreKeys.AppTheme.SYSTEM),
            accent = safeValueOf(accent, VeenaAccent.MATERIAL_YOU)
        )
    }

    val currentTheme = dataStore
        .getValueFlow(DataStoreKeys.APP_THEME_KEY, DataStoreKeys.AppTheme.SYSTEM.name)
        .map { safeValueOf(it, DataStoreKeys.AppTheme.SYSTEM) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DataStoreKeys.AppTheme.SYSTEM)


    val availableSuggestionProviders = musicRepository.availableSuggestionProviders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf(MusicRepository.ProviderItem("itunes", "iTunes"))
        )

    val currentSuggestionProvider = musicRepository.currentSuggestionProviderId
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "itunes"
        )

    fun setSuggestionProvider(providerId: String) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[SUGGESTION_PROVIDER_KEY] = providerId
            }
        }
    }

    val currentAccent = dataStore
        .getValueFlow(DataStoreKeys.APP_ACCENT_KEY, VeenaAccent.MATERIAL_YOU.name)
        .map { safeValueOf(it, VeenaAccent.MATERIAL_YOU) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VeenaAccent.MATERIAL_YOU)


    // 3. Audio Quality
    val currentQuality = dataStore
        .getValueFlow(DataStoreKeys.AUDIO_QUALITY_KEY, DataStoreKeys.AudioQuality.HIGH.name)
        .map { safeValueOf(it, DataStoreKeys.AudioQuality.HIGH) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DataStoreKeys.AudioQuality.HIGH)

    val currentDownloadQuality = dataStore
        .getValueFlow(DataStoreKeys.DOWNLOAD_QUALITY_KEY, DataStoreKeys.AudioQuality.HIGH.name)
        .map { safeValueOf(it, DataStoreKeys.AudioQuality.HIGH) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DataStoreKeys.AudioQuality.HIGH)

    fun setDownloadQuality(quality: DataStoreKeys.AudioQuality) = viewModelScope.launch {
        dataStore.saveEnum(DataStoreKeys.DOWNLOAD_QUALITY_KEY, quality)
    }


    fun setTheme(theme: DataStoreKeys.AppTheme) = viewModelScope.launch { dataStore.saveEnum(DataStoreKeys.APP_THEME_KEY, theme) }
    fun setAccent(accent: VeenaAccent) = viewModelScope.launch { dataStore.saveEnum(DataStoreKeys.APP_ACCENT_KEY, accent) }
    fun setAudioQuality(quality: DataStoreKeys.AudioQuality) =  viewModelScope.launch { dataStore.saveEnum(DataStoreKeys.AUDIO_QUALITY_KEY, quality) }

    data class InitialSettings(
        val theme: DataStoreKeys.AppTheme,
        val accent: VeenaAccent, )
}