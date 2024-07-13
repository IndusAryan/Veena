package com.aryan.veena.database

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aryan.veena.utils.CoroutineUtils.ioScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

object DataStoreHelper {
    private const val PREFERENCES_NAME = "settings_preferences"
    private val Context.dataStore by preferencesDataStore(PREFERENCES_NAME)

    val THEME_KEY = stringPreferencesKey("theme")
    const val LIGHT_THEME = "light"
    const val DARK_THEME = "dark"
    const val SYSTEM_THEME = "system"

    val ACCENT_KEY = stringPreferencesKey("accent")
    const val ACCENT_PINK = "pink"
    const val ACCENT_YELLOW = "yellow"
    const val ACCENT_MONET = "monet"

    // Returning Flow<String>: If we want to observe changes to the preference and react to updates in real-time.
    suspend fun <T> getPreferenceFlow(context: Context, key: Preferences.Key<T>, defaultValue: T): T {
        return context.dataStore.data.map { preferences ->
            preferences[key] ?: defaultValue
        }.first()
    }

    fun <T> setPreference(context: Context, key: Preferences.Key<T>, value: T) {
        ioScope {
            context.dataStore.edit { preferences ->
                preferences[key] = value
            }
        }
    }
}
