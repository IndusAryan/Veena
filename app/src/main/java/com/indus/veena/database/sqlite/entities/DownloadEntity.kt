package com.indus.veena.database.sqlite.entities

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val songId: String,
    val title: String,
    val url: String,
    val artworkUrl: String,
    val extensionName: String = "",
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val artworkData: ByteArray? = null,
    val customHeaders: String, // Stored as JSON or simple key:val,key:val string
    val artist: String = "",
    val album: String = "",
    val albumArtist: String = "",
    val composer: String = "",
    val genre: String = "",
    val lyricist: String = "",
    val year: String = "",
    val comment: String = "",
    val progress: Int = 0,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val state: DownloadState = DownloadState.PENDING,
    val savedPath: String = ""
)

enum class DownloadState {
    FETCHING, PENDING, DOWNLOADING, PAUSED, COMPLETED, FAILED
}