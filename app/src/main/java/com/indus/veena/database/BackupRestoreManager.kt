package com.indus.veena.database

import android.content.Context
import android.net.Uri
import com.indus.veena.database.sqlite.daos.DownloadDao
import com.indus.veena.database.sqlite.daos.SearchHistoryDao
import com.indus.veena.database.sqlite.entities.DownloadEntity
import com.indus.veena.database.sqlite.entities.SearchHistoryEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import javax.inject.Inject
import kotlin.collections.isNotEmpty

@Serializable
data class AppBackupData(
    val downloads: List<DownloadEntity>,
    val searchHistory: List<SearchHistoryEntity>
)

class BackupRestoreManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadDao: DownloadDao,
    private val searchHistoryDao: SearchHistoryDao
) {
    private val jsonFormat = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Exports current DB data to a JSON file at the given URI
     */
    suspend fun exportData(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Fetch Data
            val downloads = downloadDao.getAllDownloadsList()
            val history = searchHistoryDao.getAllHistoryList()

            // 2. Serialize
            val backupData = AppBackupData(downloads, history)
            val jsonString = jsonFormat.encodeToString(AppBackupData.serializer(), backupData)

            // 3. Write to File
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(jsonString)
                }
            } ?: throw IllegalStateException("Could not open output stream for $uri")

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Imports data from a JSON file at the given URI into the DB
     */
    suspend fun importData(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Read File
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    reader.readText()
                }
            } ?: throw IllegalArgumentException("Could not read file at $uri")

            // 2. Deserialize
            val backupData = jsonFormat.decodeFromString(AppBackupData.serializer(), jsonString)

            // 3. Clear Existing Data (Optional: Change to merge logic if preferred)

            // 4. Insert New Data
            if (backupData.downloads.isNotEmpty()) {
                backupData.downloads.forEach {

                downloadDao.insertOrUpdate(it)
                }
            }

            if (backupData.searchHistory.isNotEmpty()) {
                searchHistoryDao.insertHistoryList(backupData.searchHistory)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}