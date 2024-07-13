package com.aryan.veena.helpers

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.flow.Flow

object PreferenceHelper {

    private var dataStore: Preferences? = null

  /*  fun init(context: Context) {
        dataStore = PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("settings")
            }
        )
    }

    fun <T> getPreference(key: Preferences.Key<T>, defaultValue: T): Flow<T> {
        return dataStore.data.map { preferences ->
            preferences[key] ?: defaultValue
        }
    }

    suspend fun <T> setPreference(key: Preferences.Key<T>, value: T) {
        dataStore.edit { preferences ->
            preferences[key] = value
        }
    }
*/
}