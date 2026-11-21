package com.indus.veena.database

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object DataStoreKeys {
    val DISCOVERY_PROVIDER = stringPreferencesKey("discovery_provider")
    val AUTO_UPDATE_ADDONS = booleanPreferencesKey("auto_update_addons")
    enum class AppTheme {
        SYSTEM, LIGHT, DARK, AMOLED, SYSTEM_AMOLED
    }

    enum class AudioQuality {
        HIGH, MEDIUM, LOW
    }
    val SUGGESTION_PROVIDER_KEY = stringPreferencesKey("suggestion_provider")
    val APP_THEME_KEY = stringPreferencesKey("app_theme")
    val APP_ACCENT_KEY = stringPreferencesKey("app_accent")
    val AUDIO_QUALITY_KEY = stringPreferencesKey("audio_quality")
    val DOWNLOAD_QUALITY_KEY = stringPreferencesKey("download_quality_key")
}
