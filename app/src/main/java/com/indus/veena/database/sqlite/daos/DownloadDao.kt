package com.indus.veena.database.sqlite.daos

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.indus.veena.database.sqlite.entities.DownloadEntity
import com.indus.veena.database.sqlite.entities.DownloadState
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY state DESC")
    fun observeAllDownloads(): Flow<List<DownloadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(download: DownloadEntity)

    @Query("UPDATE downloads SET progress = :progress, downloadedBytes = :downloaded, totalBytes = :total, state = :state WHERE songId = :songId")
    suspend fun updateProgress(songId: String, progress: Int, downloaded: Long, total: Long, state: DownloadState)

    @Query("UPDATE downloads SET state = :state WHERE songId = :songId")
    suspend fun updateState(songId: String, state: DownloadState)

    @Query("SELECT * FROM downloads WHERE songId = :songId LIMIT 1")
    suspend fun getDownload(songId: String): DownloadEntity?

    @Query("DELETE FROM downloads WHERE songId = :songId")
    suspend fun delete(songId: String)

    @Query("SELECT * FROM downloads") suspend fun getAllDownloadsList(): List<DownloadEntity>
}