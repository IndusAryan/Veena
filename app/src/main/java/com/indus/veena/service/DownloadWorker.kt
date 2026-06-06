package com.indus.veena.service

import android.R.attr.data
import android.R.attr.type
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.pm.ServiceInfo
import android.media.MediaScannerConnection
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
import com.indus.veena.contract.ExtSong
import com.indus.veena.database.sqlite.daos.DownloadDao
import com.indus.veena.database.sqlite.entities.DownloadEntity
import com.indus.veena.database.sqlite.entities.DownloadState
import com.indus.veena.helpers.VeenaLog
import com.kyant.taglib.Picture
import com.kyant.taglib.TagLib
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.kingg22.vibrion.id3.Id3AudioWriter
import io.github.kingg22.vibrion.id3.Id3WriterBuilder
import io.github.kingg22.vibrion.id3.Id3v2v3TagFrame
import io.github.kingg22.vibrion.id3.model.AttachedPicture
import io.github.kingg22.vibrion.id3.model.AttachedPictureType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.FileSystem
import okio.Path.Companion.toPath
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
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

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "download_channel"
    private val notificationId = inputData.getString(KEY_SONG_ID)?.hashCode() ?: 100
    private val TAG = "DownloadWorker"
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val songId = inputData.getString(KEY_SONG_ID) ?: return@withContext Result.failure()
        val downloadEntity = downloadDao.getDownload(songId) ?: return@withContext Result.failure()

        createNotificationChannel()
        setForeground(createForegroundInfo(downloadEntity.title, downloadEntity.progress))

        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val veenaDir = File(downloadsDir, context.getString(R.string.app_name))
        if (!veenaDir.exists()) veenaDir.mkdirs()

        // --- 1. SANITIZE FILENAME FIRST ---
        // Fixes the empty title and hidden file Scoped Storage crash
        val rawTitle = downloadEntity.title.ifEmpty { "Unknown_Song_$songId" }
        val safeTitle = rawTitle.replace(Regex("[\\\\/:*?\"<>|]"), "_")

        // --- 2. FILE RESUME LOGIC ---
        var file = File(veenaDir, "$safeTitle.m4a")
        if (!file.exists()) {
            val mp3File = File(veenaDir, "$safeTitle.mp3")
            if (mp3File.exists()) file = mp3File
            else {
                val webmFile = File(veenaDir, "$safeTitle.webm")
                if (webmFile.exists()) file = webmFile
            }
        }

        val downloadedBytes = if (file.exists()) file.length() else 0L
        VeenaLog.d(TAG, "STAGE 1: Worker Started for [$safeTitle]. Target: ${file.name}. Resuming from bytes: $downloadedBytes")

        val requestBuilder = Request.Builder().url(downloadEntity.url)
        if (downloadedBytes > 0) requestBuilder.addHeader("Range", "bytes=$downloadedBytes-")

        downloadEntity.customHeaders.split(";").filter { it.isNotEmpty() }.forEach {
            val parts = it.split(":", limit = 2)
            if (parts.size == 2) requestBuilder.addHeader(parts[0].trim(), parts[1].trim())
        }

        try {
            downloadDao.updateState(songId, DownloadState.DOWNLOADING)
            val response = okHttpClient.newCall(requestBuilder.build()).execute()

            if (!response.isSuccessful) {
                if (response.code == 416 && downloadedBytes > 0) {
                    // Start conversion if we resumed a fully downloaded WebM
                    handleConversionAndFinish(songId, file, safeTitle, veenaDir, downloadEntity)
                    return@withContext Result.success()
                }
                throw IOException("Unexpected code $response")
            }

            // --- 3. PROPER EXTENSION RESOLUTION ---
            val contentType = response.header("Content-Type", "") ?: ""
            val contentLength = response.header("Content-Length", "0")?.toLong() ?: 0L
            VeenaLog.d(TAG, "STAGE 2: Response Received. MIME: $contentType | Size: ${contentLength / 1024} KB")
            val extension = when {
                contentType.contains("webm") || contentType.contains("opus") -> ".webm"
                contentType.contains("audio/mpeg") -> ".mp3"
                else -> ".m4a"
            }
            val finalFile = getUniqueFile(veenaDir, safeTitle, extension)
            //val finalFile = File(veenaDir, "$safeTitle$extension")
            VeenaLog.d(TAG, "STAGE 2: Received Network Response. MIME: $contentType, Ext: $extension")

            if (file.exists() && file.absolutePath != finalFile.absolutePath) {
                file.renameTo(finalFile)
                file = finalFile
            } else {
                file = finalFile
            }

            val body = response.body
            val totalBytes = downloadedBytes + body.contentLength()

            RandomAccessFile(file, "rw").use { randomAccessFile ->
                randomAccessFile.seek(downloadedBytes)
                body.byteStream().use { inputStream ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    var currentDownloaded = downloadedBytes
                    var lastUpdate = System.currentTimeMillis()

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        ensureActive()
                        randomAccessFile.write(buffer, 0, bytesRead)
                        currentDownloaded += bytesRead

                        if (System.currentTimeMillis() - lastUpdate > 500) {
                            val progress = ((currentDownloaded * 100) / totalBytes).toInt()
                            downloadDao.updateProgress(songId, progress, currentDownloaded, totalBytes, DownloadState.DOWNLOADING)
                            setForeground(createForegroundInfo(downloadEntity.title, progress))
                            lastUpdate = System.currentTimeMillis()
                        }
                    }
                }
            }

            VeenaLog.d(TAG, "STAGE 3: Network download complete. File saved to: ${file.absolutePath}")
            downloadDao.updateProgress(songId, 100, totalBytes, totalBytes, DownloadState.DOWNLOADING)
            // Hand off to Transformer and Tagging
            handleConversionAndFinish(songId, file, safeTitle, veenaDir, downloadEntity)
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

    // Extracted logic to keep doWork clean
    private suspend fun handleConversionAndFinish(songId: String, currentFile: File, safeTitle: String, dir: File, entity: DownloadEntity) {
        var finalWorkingFile = currentFile
        val ext = currentFile.extension.lowercase()

        // Normalize WebM (Transcode) AND M4A (Remux/Standardize)
        // This fixes the "Unknown Tags" and corruption in M4A files
        if (ext == "webm" || ext == "m4a") {
            val typeLabel = if (ext == "webm") "WebM (Converting)" else "M4A (Normalizing)"
            VeenaLog.d(TAG, "STAGE 3.5: $typeLabel for [${entity.title}]...")

            // We use a hidden ".tmp" extension so it doesn't show up in music players mid-process
            val tempFile = File(dir, "${safeTitle}.tmp")
            val cleanFinalFile = File(dir, "${safeTitle}.m4a")
            val success = transcodeWebmToM4a(context, currentFile.absolutePath, tempFile.absolutePath)
            if (success && tempFile.exists()) {
                VeenaLog.d(TAG, "STAGE 3.6: Standardization Successful. Cleaning up...")
                // Delete the raw/original file (e.g., the .webm)
                currentFile.delete()
                // Rename the temp file to the proper clean name (e.g., "Song Title.m4a")
                finalWorkingFile = if (tempFile.renameTo(cleanFinalFile)) {
                    cleanFinalFile
                } else {
                    // Fallback if rename fails
                    tempFile
                }
                delay(300)
            }
        }
        finishDownload(songId, finalWorkingFile.absolutePath, entity)
    }

    private suspend fun finishDownload(songId: String, path: String, entity: DownloadEntity) {
        val file = File(path)
        if (!file.exists()) return
        downloadDao.updateProgress(songId, 100, entity.totalBytes, entity.totalBytes, DownloadState.COMPLETED)
        downloadDao.insertOrUpdate(entity.copy(savedPath = path, state = DownloadState.COMPLETED, progress = 100))

        // Placeholder for ID3 tagging
        tagId3Data(path, entity)
        //applyMetadata(path, entity)
        // 1. Physical File Fix (Still good for local file explorers)
        val now = System.currentTimeMillis()
        file.setLastModified(now)
        // 2. Database Fix (For Music Players)
        // We scan the file first, then update its DB entry
        MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null) { _, uri ->
            if (uri != null) {
                try {
                    val values = ContentValues().apply {
                        // DATE_MODIFIED is in seconds, not milliseconds
                        put(MediaStore.MediaColumns.DATE_MODIFIED, now / 1000)
                        put(MediaStore.MediaColumns.DATE_ADDED, now / 1000)
                    }
                    context.contentResolver.update(uri, values, null, null)
                    VeenaLog.d(TAG, "MediaStore Date Updated for: ${file.name}")
                } catch (e: Exception) {
                    VeenaLog.e(TAG, "Failed to update MediaStore timestamp", e)
                }
            }
        }

        notificationManager.notify(
            notificationId,
            NotificationCompat.Builder(context, channelId)
                .setContentTitle(entity.title)
                .setContentText("Download complete")
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .build()
        )
    }

    private suspend fun applyMetadata(filePath: String, entity: DownloadEntity) {
        withContext(Dispatchers.IO) {
            try {
                VeenaLog.d(TAG, "STAGE 4: Starting vibrion-id3 Tagging for [${entity.title}]")
                val file = File(filePath)
                if (!file.exists()) {
                    VeenaLog.e(TAG, "STAGE 4 ERROR: File does not exist at $filePath")
                    return@withContext
                }

                // --- 1. SMART ARTWORK RESOLUTION ---
                var pictureData: ByteArray? = entity.artworkData
                if (pictureData == null && entity.artworkUrl.isNotEmpty()) {
                    VeenaLog.d(TAG, "STAGE 4.1: No Blob found. Fetching artwork from URL...")
                    val imgRequest = Request.Builder().url(entity.artworkUrl).build()
                    val imgResponse = okHttpClient.newCall(imgRequest).execute()
                    if (imgResponse.isSuccessful) {
                        pictureData = imgResponse.body?.bytes()
                        VeenaLog.d(TAG, "STAGE 4.2: Artwork fetched successfully from network.")
                    }
                }

                // --- 2. GENERATE ID3 TAG BYTES ---
                VeenaLog.d(TAG, "STAGE 4.3: Generating ID3 Tag Bytes...")
                val tagBytes: ByteArray = Id3WriterBuilder.id3Writer {
                    // DSL based on the library documentation
                    title = entity.title

                    if (entity.artist.isNotBlank()) artist(entity.artist)
                    if (entity.album.isNotBlank()) album = entity.album
                    if (entity.year.isNotBlank()) {
                        entity.year.toIntOrNull()?.let { year = it }
                    }

                    // Handle Picture (Cover)
                    pictureData?.let { dataBytes ->
                        picture {
                            type = AttachedPictureType.CoverFront
                            data = dataBytes
                        }
                    }
                }.toByteArray()

                // --- 3. READ RAW AUDIO & STRIP EXISTING TAGS ---
                VeenaLog.d(TAG, "STAGE 4.4: Reading audio and stripping old tags...")
                // Note: file.readBytes() loads the whole file into RAM.
                // This is required by vibrion-id3's `removeTag` implementation.
                val rawAudioBytes = file.readBytes()
                val cleanAudio = Id3AudioWriter.removeTag(rawAudioBytes)

                // --- 4. OVERWRITE FILE: [NEW TAG] + [CLEAN AUDIO] ---
                VeenaLog.d(TAG, "STAGE 4.5: Writing new tagged file...")
                file.outputStream().use { fos ->
                    fos.write(tagBytes)
                    fos.write(cleanAudio)
                }

                VeenaLog.d(TAG, "STAGE 5: vibrion-id3 Tagging complete! File is ready.")

            } catch (e: Exception) {
                VeenaLog.e(TAG, "ERROR @ STAGE 4: vibrion-id3 Tagging process failed", e)
            }
        }
    }

    private suspend fun tagId3Data(filePath: String, entity: DownloadEntity) {
        withContext(Dispatchers.IO) {
            try {
                VeenaLog.d(TAG, "STAGE 4: Starting ID3 Tagging for [${entity.title}]")
                val file = File(filePath)
                // --- 1. SMART ARTWORK RESOLUTION ---
                var pictureData: ByteArray? = entity.artworkData // Check DB Blob first!
                var mimeType = "image/jpeg"

                if (pictureData != null) {
                    VeenaLog.d(TAG, "STAGE 4.1: Using cached Artwork Blob from DB. Skipping network call.")
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

                // --- 2. TAG INJECTION ---
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE).use { pfd ->
                    val fdForMeta = pfd.dup().detachFd()
                    val metadata = TagLib.getMetadata(fdForMeta, readPictures = true)

                    if (metadata != null) {
                        VeenaLog.d(TAG, "STAGE 4.3: Injecting Text Tags...")
                        val newProps = metadata.propertyMap.apply {
                            VeenaLog.d(TAG, "TAG DATA PREVIEW: " +
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
                            if (entity.composer.isNotBlank()) this["COMPOSER"] = arrayOf(entity.composer)
                            if (entity.genre.isNotBlank()) this["GENRE"] = arrayOf(entity.genre.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(
                                    Locale.getDefault()
                                ) else it.toString()
                            })
                            if (entity.lyricist.isNotBlank()) this["LYRICIST"] = arrayOf(entity.lyricist)
                            if (entity.year.isNotBlank()) {
                                this["DATE"] = arrayOf(entity.year)
                                this["YEAR"] = arrayOf(entity.year)
                            }
                            if (entity.comment.isNotBlank()) this["COMMENT"] = arrayOf(entity.comment)
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
                        VeenaLog.d(TAG, "STAGE 5: Tagging complete! File is perfectly tagged and ready.")
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
    The "Conversion Trap": If your app needs an AAC file but you download the 160 kbps Opus stream and convert it to 128 kbps AAC, the quality will be worse than if you just downloaded the 128 kbps AAC file directly. This is because you are compressing a file that has already been compressed once (re-encoding), which introduces digital artifacts and "generation loss".
    */
    @OptIn(UnstableApi::class)
    private suspend fun transcodeWebmToM4a(context: Context, inputPath: String, outputPath: String): Boolean {
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
                    override fun onError(composition: Composition, exportResult: ExportResult, e: ExportException) {
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

    private fun getUniqueFile(dir: File, baseName: String, extension: String): File {
        var file = File(dir, "$baseName$extension")
        var counter = 1
        while (file.exists()) {
            file = File(dir, "$baseName ($counter)$extension")
            counter++
        }
        return file
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