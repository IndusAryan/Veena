package com.indus.veena.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.indus.veena.providers.MainMusicProvider
import com.indus.veena.repository.MusicRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "veena_prefs")

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    suspend fun <T> DataStore<Preferences>.setValue(key: Preferences.Key<T>, value: T) {
        this.edit { preferences ->
            preferences[key] = value
        }
    }

    /**
     * 2. SYNC/REACTIVE GETTER (Flow)
     * Returns a Flow that emits whenever the value changes.
     */
    fun <T> DataStore<Preferences>.getValueFlow(key: Preferences.Key<T>, defaultValue: T): Flow<T> {
        return this.data
            .map { preferences ->
                preferences[key] ?: defaultValue
            }
    }

    /**
     * 3. ASYNC GETTER (One-shot Snapshot)
     * Fetches the current value once without subscribing.
     */
    suspend fun <T> DataStore<Preferences>.getSnapshot(key: Preferences.Key<T>, defaultValue: T): T {
        return this.data
            .map { preferences ->
                preferences[key] ?: defaultValue
            }
            .first()
    }

    /**
     * 4. CLEAR SPECIFIC KEY
     */
    suspend fun <T> DataStore<Preferences>.removeKey(key: Preferences.Key<T>) {
        this.edit { it.remove(key) }
    }
}