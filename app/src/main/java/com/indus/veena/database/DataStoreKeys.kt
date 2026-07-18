package com.indus.veena.database

import androidx.datastore.preferences.core.stringPreferencesKey

object DataStoreKeys {
    enum class AppTheme {
        SYSTEM, LIGHT, DARK, AMOLED
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