package com.indus.veena.helpers

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import coil3.BitmapImage
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

object ImageModuleCoil {
    suspend fun extractColorsFromBitmap(context: PlatformContext, imageUrl: String): Color {
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .allowHardware(false)
            .build()

        val result = loader.execute(request)
        return if (result is SuccessResult) {
            val bitmap = (result.image as? BitmapImage)?.bitmap
            if (bitmap != null) {
                val palette = Palette.from(bitmap).generate()
                val swatch = palette.mutedSwatch ?: palette.dominantSwatch
                swatch?.rgb?.let { Color(it) } ?: Color(0xFF1A1616)
            } else Color(0xFF1A1616)
        } else Color(0xFF1A1616)
    }

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
}