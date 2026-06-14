package com.indus.veena.helpers

import android.content.Context
import android.graphics.Bitmap
import android.os.Build.VERSION.SDK_INT
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.palette.graphics.Palette
import coil3.BitmapImage
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.disk.DiskCache
import coil3.imageLoader
import coil3.memory.MemoryCache
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.request.crossfade
import coil3.util.DebugLogger
import com.indus.veena.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Path.Companion.toOkioPath
import java.io.ByteArrayOutputStream

object ImageModuleCoil {

    internal fun buildImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .crossfade(200)
            .allowHardware(SDK_INT >= 28)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .memoryCache {
                MemoryCache.Builder().maxSizePercent(context, 0.1)//10 % of heap for mem-cache
                    .strongReferencesEnabled(false)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("veena_coil_cache").toOkioPath())
                    .maxSizeBytes(512L * 1024 * 1024) // 512 MB
                    .maxSizePercent(0.04) // max 4% of storage for disk caching
                    .build()
            }
            .logger(if (BuildConfig.DEBUG) DebugLogger() else null)
            .build()
    }

    @Composable
    fun LoadImage(
        model: Any?,
        contentDescription: String?,
        modifier: Modifier = Modifier,
        placeholder: Painter? = null,
        error: Painter? = null,
        placeholderVector: ImageVector? = null,
        errorVector: ImageVector? = null,
        headers: NetworkHeaders? = null,
        onSuccess: ((result: AsyncImagePainter.State.Success) -> Unit)? = null,
        onError: ((result: AsyncImagePainter.State.Error) -> Unit)? = null,
    ) {
        val context = LocalPlatformContext.current
        val finalPlaceholder: Painter? = placeholder
            ?: if (placeholderVector != null) {
                rememberVectorPainter(image = placeholderVector)
            } else {
                null
            }

        val finalError: Painter? = error
            ?: if (errorVector != null) {
                rememberVectorPainter(image = errorVector)
            } else {
                null
            }
        val imageRequest = remember(model, headers) {
            ImageRequest.Builder(context)
                .data(model)
                .apply {
                    NetworkHeaders.Builder()
                        .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36")
                        .build()
                    headers?.let {
                        httpHeaders(it)
                    }
                }
                .crossfade(true)
                .build()
        }

        AsyncImage(
            model = imageRequest,
            contentDescription = contentDescription,
            modifier = modifier,
            placeholder = finalPlaceholder,
            error = finalError,
            onSuccess = { state ->
                onSuccess?.invoke(state)
            },
            onError = { state ->
                onError?.invoke(state)
            }
        )
    }

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