// ImageLoaderSingleton.kt

package com.aryan.veena.utils

import android.content.Context
import coil.ImageLoader
import coil.memory.MemoryCache
import coil.request.CachePolicy

object ImageLoader {

    private var INSTANCE: ImageLoader? = null

    fun getInstance(context: Context): ImageLoader {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: buildImageLoader(context).also { INSTANCE = it }
        }
    }

    private fun buildImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .crossfade(true)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25) // Use 25% of the app's available memory for image caching
                    .build()
            }
            .diskCache {
                coil.disk.DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02) // Use 2% of the device's storage space for disk caching
                    .build()
            }
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
    }
}
