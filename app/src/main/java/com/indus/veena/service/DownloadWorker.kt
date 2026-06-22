package com.indus.veena.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.indus.veena.R
import com.indus.veena.database.sqlite.daos.DownloadDao
import com.indus.veena.database.sqlite.entities.DownloadEntity
import com.indus.veena.database.sqlite.entities.DownloadState
import com.indus.veena.helpers.VeenaLog
import com.kyant.taglib.Picture
import com.kyant.taglib.TagLib
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume

@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val okHttpClient: OkHttpClient,
    private val downloadDao: DownloadDao
) : CoroutineWorker(context, workerParams) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "download_channel"
    private val notificationId = inputData.getString(KEY_SONG_ID)?.hashCode() ?: 100
    private val TAG = "DownloadWorker"
    // Sub-folder name under Downloads, e.g. "Downloads/Veena"
    private val publicSubDir get() = context.getString(R.string.app_name)


    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val songId = inputData.getString(KEY_SONG_ID) ?: return@withContext Result.failure()
        val downloadEntity = downloadDao.getDownload(songId) ?: return@withContext Result.failure()
        // Sanitize for filesystem use — songId itself may be a URL (e.g. "https://...")
        val safeSongId = songId.replace(Regex("[\\\\/:*?\"<>|]"), "_")

        createNotificationChannel()
        setForeground(createForegroundInfo(downloadEntity.title, downloadEntity.progress))

        val workDir = File(context.cacheDir, "downloads").apply { if (!exists()) mkdirs() }

        val rawTitle = downloadEntity.title.ifEmpty { "Unknown_Song_$safeSongId" }
        val safeTitle = rawTitle.replace(Regex("[\\\\/:*?\"<>|]"), "_")

        val requestBuilder = Request.Builder().url(downloadEntity.url)
        requestBuilder.addHeader("Range", "bytes=0-")
        downloadEntity.customHeaders.split(";").filter { it.isNotEmpty() }.forEach {
            val parts = it.split(":", limit = 2)
            if (parts.size == 2) requestBuilder.addHeader(parts[0].trim(), parts[1].trim())
        }

        try {
            downloadDao.updateState(songId, DownloadState.DOWNLOADING)
            val response = okHttpClient.newCall(requestBuilder.build()).execute()

            if (!response.isSuccessful) {
                throw IOException("Unexpected code $response")
            }

            val contentType = response.header("Content-Type", "") ?: ""
            val contentLength = response.header("Content-Length", "0")?.toLong() ?: 0L
            VeenaLog.d(
                TAG,
                "STAGE 2: Response Received. MIME: $contentType | Size: ${contentLength / 1024} KB"
            )
            val extension = when {
                contentType.contains("webm") || contentType.contains("opus") -> ".webm"
                contentType.contains("audio/mpeg") -> ".mp3"
                else -> ".m4a"
            }
            val workingFile = File(workDir, "${safeSongId}_raw$extension")

            val body = response.body
            val totalBytes = body.contentLength()

            FileOutputStream(workingFile).use { fos ->
                body.byteStream().use { inputStream ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    var currentDownloaded = 0L
                    var lastUpdate = System.currentTimeMillis()

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        ensureActive()
                        fos.write(buffer, 0, bytesRead)
                        currentDownloaded += bytesRead

                        if (System.currentTimeMillis() - lastUpdate > 500) {
                            val progress = if (totalBytes > 0)
                                ((currentDownloaded * 100) / totalBytes).toInt() else 0
                            downloadDao.updateProgress(
                                songId,
                                progress,
                                currentDownloaded,
                                totalBytes,
                                DownloadState.DOWNLOADING
                            )
                            setForeground(createForegroundInfo(downloadEntity.title, progress))
                            lastUpdate = System.currentTimeMillis()
                        }
                    }
                }
            }

            VeenaLog.d(TAG, "STAGE 3: Download complete. Cached at: ${workingFile.absolutePath}")
            downloadDao.updateProgress(songId, 100, totalBytes, totalBytes, DownloadState.DOWNLOADING)
            handleConversionAndFinish(songId, safeSongId, workingFile, safeTitle, workDir, downloadEntity)
            return@withContext Result.success()
        } catch (e: CancellationException) {
            VeenaLog.d(TAG, "STAGE 3.1: Download paused/cancelled for [${downloadEntity.title}]")
            downloadDao.updateState(songId, DownloadState.PAUSED)
            return@withContext Result.success()
        } catch (e: Exception) {
            VeenaLog.e(TAG, "ERROR: Download failed for [${downloadEntity.title}]", e)
            downloadDao.updateState(songId, DownloadState.FAILED)
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Download Failed: ${downloadEntity.title}\n${e.localizedMessage ?: "Unknown error"}",
                    Toast.LENGTH_LONG
                ).show()
            }
            return@withContext Result.failure()
        }
    }

    private suspend fun handleConversionAndFinish(
        songId: String,
        safeSongId: String,
        currentFile: File,
        safeTitle: String,
        workDir: File,
        entity: DownloadEntity
    ) {
        val ext = currentFile.extension.lowercase()

        // Normalize WebM (Transcode) AND M4A (Remux/Standardize) — all still in cacheDir.
        VeenaLog.d(TAG, "STAGE 3.5: Normalizing/Converting for [${entity.title}]...")
        val tempFile = File(workDir, "${safeSongId}_out.m4a")
        val success = transcodeWebmToM4a(context, currentFile.absolutePath, tempFile.absolutePath)

        val finalWorkingFile: File = if (success && tempFile.exists()) {
            VeenaLog.d(TAG, "STAGE 3.6: Standardization Successful.")
            currentFile.delete()
            tempFile
        } else if (ext == "m4a" || ext == "mp3") {
            // Transcode failed but original is already a usable container — fall back to it.
            VeenaLog.d(TAG, "STAGE 3.6: Transcode skipped/failed, using original container.")
            currentFile
        } else {
            VeenaLog.e(TAG, "STAGE 3.6: Transcode failed and source format unusable.")
            downloadDao.updateState(songId, DownloadState.FAILED)
            return
        }

        // Tag the file while it's still a plain cache file — full random read/write access.
        tagId3Data(finalWorkingFile, entity)

        // Publish to public Downloads/<AppName>/ via MediaStore.
        val publishedUri = publishToDownloads(finalWorkingFile, safeTitle, songId)
        finalWorkingFile.delete()

        if (publishedUri == null) {
            VeenaLog.e(TAG, "ERROR: Failed to publish file to MediaStore")
            downloadDao.updateState(songId, DownloadState.FAILED)
            return
        }

        finishDownload(songId, publishedUri, entity)
    }

    /**
     * Copies the finished, tagged file into the public Downloads/<AppName>/ directory
     * via MediaStore. Works on all API levels without WRITE_EXTERNAL_STORAGE or
     * MANAGE_EXTERNAL_STORAGE, since the app is only writing a file it created itself.
     */
    private fun publishToDownloads(sourceFile: File, safeTitle: String, songId: String): Uri? {
        val resolver = context.contentResolver
        val displayName = getUniqueDisplayName(resolver, safeTitle)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, displayName)
                put(MediaStore.Downloads.MIME_TYPE, "audio/mp4")
                put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_MUSIC}/$publicSubDir")
                put(MediaStore.Downloads.IS_PENDING, 1)
                put(MediaStore.Audio.Media.TITLE, safeTitle)
                put(MediaStore.Audio.Media.IS_MUSIC, 1)
            }
            val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values) ?: return null

            try {
                resolver.openOutputStream(uri)?.use { out ->
                    sourceFile.inputStream().use { it.copyTo(out) }
                } ?: return null

                values.clear()
                values.put(MediaStore.Audio.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                uri
            } catch (e: Exception) {
                VeenaLog.e(TAG, "Failed to publish file", e)
                resolver.delete(uri, null, null)
                null
            }
        } else {
            // Pre-Q: write directly to the public Downloads dir. This is allowed without
            // any runtime permission because WRITE_EXTERNAL_STORAGE is normal-protection
            // and granted at install time on API < 29 (and irrelevant on API < 19).
            @Suppress("DEPRECATION")
            val downloadsDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                publicSubDir
            )
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val destFile = File(downloadsDir, displayName)
            try {
                sourceFile.inputStream().use { input ->
                    destFile.outputStream().use { output -> input.copyTo(output) }
                }
                val values = ContentValues().apply {
                    put(MediaStore.Audio.Media.DATA, destFile.absolutePath)
                    put(MediaStore.Audio.Media.DISPLAY_NAME, displayName)
                    put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp4")
                }
                resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
                    ?: Uri.fromFile(destFile)
            } catch (e: Exception) {
                VeenaLog.e(TAG, "Failed to publish file (pre-Q)", e)
                null
            }
        }
    }

    /** Avoids collisions in the target dir by appending " (1)", " (2)", etc. */
    private fun getUniqueDisplayName(resolver: ContentResolver, baseName: String): String {
        var candidate = "$baseName.m4a"
        var counter = 1
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            while (true) {
                val cursor = resolver.query(
                    collection,
                    arrayOf(MediaStore.Audio.Media._ID),
                    "${MediaStore.Audio.Media.DISPLAY_NAME}=? AND ${MediaStore.Audio.Media.RELATIVE_PATH}=?",
                    arrayOf(
                        candidate,
                        "${Environment.DIRECTORY_MUSIC}/$publicSubDir/"
                    ),
                    null
                )
                val exists = cursor?.use { it.count > 0 } ?: false
                if (!exists) break
                candidate = "$baseName ($counter).m4a"
                counter++
            }
        } else {
            @Suppress("DEPRECATION")
            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                publicSubDir
            )
            while (File(dir, candidate).exists()) {
                candidate = "$baseName ($counter).m4a"
                counter++
            }
        }
        return candidate
    }

    private suspend fun finishDownload(songId: String, publishedUri: Uri, entity: DownloadEntity) {
        downloadDao.updateProgress(songId, 100, entity.totalBytes, entity.totalBytes, DownloadState.COMPLETED)
        downloadDao.insertOrUpdate(
            entity.copy(
                savedPath = publishedUri.toString(),
                state = DownloadState.COMPLETED,
                progress = 100
            )
        )

        notificationManager.notify(
            notificationId,
            NotificationCompat.Builder(context, channelId)
                .setContentTitle(entity.title)
                .setContentText("Download complete")
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .build()
        )
    }

    private suspend fun tagId3Data(file: File, entity: DownloadEntity) {
        withContext(Dispatchers.IO) {
            try {
                VeenaLog.d(TAG, "STAGE 4: Starting ID3 Tagging for [${entity.title}]")
                var pictureData: ByteArray? = entity.artworkData
                var mimeType = "image/jpeg"

                if (pictureData != null) {
                    VeenaLog.d(
                        TAG,
                        "STAGE 4.1: Using cached Artwork Blob from DB. Skipping network call."
                    )
                } else if (entity.artworkUrl.isNotEmpty()) {
                    VeenaLog.d(TAG, "STAGE 4.1: No Blob found. Fetching artwork from URL...")
                    val imgRequest = Request.Builder().url(entity.artworkUrl).build()
                    val imgResponse = okHttpClient.newCall(imgRequest).execute()
                    if (imgResponse.isSuccessful) {
                        pictureData = imgResponse.body.bytes()
                        mimeType = imgResponse.header("Content-Type", "image/jpeg") ?: "image/jpeg"
                        VeenaLog.d(TAG, "STAGE 4.2: Artwork fetched successfully from network.")
                    }
                }

                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE).use { pfd ->
                    val fdForMeta = pfd.dup().detachFd()
                    val metadata = TagLib.getMetadata(fdForMeta, readPictures = true)

                    if (metadata != null) {
                        VeenaLog.d(TAG, "STAGE 4.3: Injecting Text Tags...")
                        val newProps = metadata.propertyMap.apply {
                            VeenaLog.d(
                                TAG, "TAG DATA PREVIEW: " +
                                        "Title: '${entity.title}' | " +
                                        "Artist: '${entity.artist}' | " +
                                        "Album: '${entity.album}' | " +
                                        "Date/Year: '${entity.year}' | " +
                                        "Genre: '${entity.genre}' | " +
                                        "Album artist (band): '${entity.albumArtist}' | " +
                                        "Composer: '${entity.composer}' | " +
                                        "Genre: '${entity.genre}' | " +
                                        "Lyricist: '${entity.lyricist}' | " +
                                        "Comment: '${entity.comment}'"
                            )
                            if (entity.title.isNotBlank()) this["TITLE"] = arrayOf(entity.title)
                            if (entity.artist.isNotBlank()) this["ARTIST"] = arrayOf(entity.artist)
                            if (entity.album.isNotBlank()) this["ALBUM"] = arrayOf(entity.album)
                            if (entity.albumArtist.isNotBlank()) {
                                this["ALBUMARTIST"] = arrayOf(entity.albumArtist)
                                this["BAND"] = arrayOf(entity.albumArtist)
                            }
                            if (entity.composer.isNotBlank()) this["COMPOSER"] =
                                arrayOf(entity.composer)
                            if (entity.genre.isNotBlank()) this["GENRE"] =
                                arrayOf(entity.genre.replaceFirstChar {
                                    if (it.isLowerCase()) it.titlecase(
                                        Locale.getDefault()
                                    ) else it.toString()
                                })
                            if (entity.lyricist.isNotBlank()) this["LYRICIST"] =
                                arrayOf(entity.lyricist)
                            if (entity.year.isNotBlank()) {
                                this["DATE"] = arrayOf(entity.year)
                                this["YEAR"] = arrayOf(entity.year)
                            }
                            if (entity.comment.isNotBlank()) this["COMMENT"] =
                                arrayOf(entity.comment)
                        }

                        val fdForProps = pfd.dup().detachFd()
                        TagLib.savePropertyMap(fdForProps, newProps)

                        if (pictureData != null) {
                            VeenaLog.d(TAG, "STAGE 4.4: Injecting Cover Art Tag...")
                            val picture = Picture(
                                data = pictureData,
                                description = "Cover",
                                pictureType = "Front Cover",
                                mimeType = mimeType
                            )
                            val fdForPic = pfd.dup().detachFd()
                            TagLib.savePictures(fdForPic, arrayOf(picture))
                        }
                        VeenaLog.d(
                            TAG,
                            "STAGE 5: Tagging complete! File is perfectly tagged and ready."
                        )
                    }
                }
            } catch (e: Exception) {
                VeenaLog.e(TAG, "ERROR @ STAGE 4: Tagging process failed", e)
            }
        }
    }

    private fun createForegroundInfo(title: String, progress: Int): ForegroundInfo {
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Downloading $title")
            .setProgress(100, progress, false)
            .setSmallIcon(R.drawable.ic_downloading)
            .setOngoing(true)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                notificationId,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }

    /*
    Itag 140 (AAC ~128 kbps): This is the highest-quality standard AAC stream YouTube provides for most videos.
    Itag 251 (Opus ~160 kbps): This is often the highest-quality overall audio stream YouTube offers.
    no conversion: we need AAC file but if we download the 160 kbps Opus stream and convert it to 128 kbps AAC,
    the quality will be worse than the org 128 kbps AAC file directly due to recompression and re-encoding,
    which introduces digital artifacts and "generation loss" and a disgrace to your ancestors.
    */
    @OptIn(UnstableApi::class)
    private suspend fun transcodeWebmToM4a(
        context: Context,
        inputPath: String,
        outputPath: String
    ): Boolean {
        return withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                val transformer = Transformer.Builder(context)
                    .setAudioMimeType(MimeTypes.AUDIO_AAC) // Native M4A audio
                    .build()

                val mediaItem = MediaItem.fromUri(Uri.fromFile(File(inputPath)))

                val editedMediaItem = EditedMediaItem.Builder(mediaItem)
                    .setRemoveVideo(true)
                    .build()

                transformer.addListener(object : Transformer.Listener {
                    override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                        if (continuation.isActive) continuation.resume(true)
                    }

                    override fun onError(
                        composition: Composition,
                        exportResult: ExportResult,
                        e: ExportException
                    ) {
                        VeenaLog.e("DownloadWorker", "Transformer Error: ${e.message}")
                        if (continuation.isActive) continuation.resume(false)
                    }
                })

                transformer.start(editedMediaItem, outputPath)

                continuation.invokeOnCancellation {
                    Handler(Looper.getMainLooper()).post { transformer.cancel() }
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, "Downloads", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val KEY_SONG_ID = "song_id"
    }
}