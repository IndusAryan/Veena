package com.indus.veena.service

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

@UnstableApi
object MediaCache {
    private const val CACHE_DIR_NAME = "veena_media_cache"
    private const val MAX_CACHE_SIZE_BYTES = 256 * 1024 * 1024L
    private var cacheInstance: SimpleCache? = null
    @Synchronized
    fun getCache(context: Context): SimpleCache {
        val currentInstance = cacheInstance
        if (currentInstance != null) return currentInstance
        val appContext = context.applicationContext
        val cacheDir = File(appContext.cacheDir, CACHE_DIR_NAME)
        val evictor = LeastRecentlyUsedCacheEvictor(MAX_CACHE_SIZE_BYTES)
        val databaseProvider = StandaloneDatabaseProvider(appContext)
        return SimpleCache(cacheDir, evictor, databaseProvider).also { cacheInstance = it }
    }
}