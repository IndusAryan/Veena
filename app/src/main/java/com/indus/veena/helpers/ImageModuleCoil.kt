package com.indus.veena.helpers

import android.content.Context
import android.graphics.Bitmap
import coil3.BitmapImage
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

suspend fun Context.getCachedArtworkBlob(url: String): ByteArray? = withContext(Dispatchers.IO) {
    if (url.isEmpty()) return@withContext null

    val request = ImageRequest.Builder(this@getCachedArtworkBlob)
        .data(url)
        .allowHardware(false) // Prevent crashes when converting hardware bitmaps to bytes
        .build()

    val result = imageLoader.execute(request).image
    val bitmap = (result as? BitmapImage)?.bitmap ?: return@withContext null

    ByteArrayOutputStream().use { stream ->
        bitmap.compress(Bitmap.CompressFormat.WEBP, 80, stream)
        stream.toByteArray()
    }
}