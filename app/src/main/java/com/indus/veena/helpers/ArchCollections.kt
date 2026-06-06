package com.indus.veena.helpers

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.indus.veena.di.AppModule.setValue

suspend fun <T : Enum<T>> DataStore<Preferences>.saveEnum(key: Preferences.Key<String>, value: T) {
     setValue(key, value.name)
}

inline fun <reified T : Enum<T>> safeValueOf(name: String, default: T): T {
    return try { enumValueOf<T>(name) } catch (e: Exception) { default }
}