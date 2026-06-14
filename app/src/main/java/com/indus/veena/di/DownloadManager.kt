package com.indus.veena.di

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.indus.veena.database.sqlite.daos.DownloadDao
import com.indus.veena.database.sqlite.entities.DownloadEntity
import com.indus.veena.database.sqlite.entities.DownloadState
import com.indus.veena.models.SongModel
import com.indus.veena.service.DownloadWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.emptyMap

@Singleton
class DownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadDao: DownloadDao
) {
    private val workManager = WorkManager.getInstance(context)
    val activeDownloads: Flow<List<DownloadEntity>> = downloadDao.observeAllDownloads()

    suspend fun startDownload(
        song: SongModel,
        streamUrl: String,
        artworkData: ByteArray? = null, // Optional fast-path for image
        customHeaders: Map<String, String> = mapOf("Range" to "bytes=0-")
    ) {
        val headersString = customHeaders.entries.joinToString(";") { "${it.key}:${it.value}" }

        val entity = DownloadEntity(
            songId = song.id,
            title = song.title,
            url = streamUrl,
            artworkUrl = song.thumbnail,
            extensionName = song.extensionName,
            artworkData = artworkData,
            customHeaders = headersString,
            artist = song.artist,
            album = song.album,
            albumArtist = song.albumArtist ?: song.artist,
            composer = song.composer,
            genre = song.genre,
            lyricist = song.lyricist ?: "",
            year = song.year,
            comment = "Veena: ${song.provider.uppercase()}",
            state = DownloadState.PENDING
        )
        downloadDao.insertOrUpdate(entity)
        enqueueWork(song.id)
    }

    suspend fun prepareDownloadEntry(song: SongModel) {
        val entity = DownloadEntity(
            songId = song.id,
            title = song.title,
            url = "",
            state = DownloadState.FETCHING,
            artworkUrl = "",
            extensionName = song.extensionName,
            customHeaders = "",
        )
        downloadDao.insertOrUpdate(entity)
    }

    suspend fun pauseDownload(songId: String) {
        workManager.cancelUniqueWork(songId)
        downloadDao.updateState(songId, DownloadState.PAUSED)
    }

    suspend fun resumeDownload(songId: String) {
        downloadDao.updateState(songId, DownloadState.PENDING)
        enqueueWork(songId)
    }

    suspend fun removeDownload(songId: String) {
        workManager.cancelUniqueWork(songId)
        val entity = downloadDao.getDownload(songId)
        entity?.savedPath?.let { path ->
            val file = File(path)
            if (file.exists()) file.delete()
        }
        downloadDao.delete(songId)
    }

    private fun enqueueWork(songId: String) {
        val inputData = workDataOf(DownloadWorker.KEY_SONG_ID to songId)
        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInputData(inputData)
            .build()
        workManager.enqueueUniqueWork(
            songId,
            ExistingWorkPolicy.KEEP,
            request
        )
    }
}