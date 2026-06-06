package com.indus.veena.database.sqlite.daos

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import com.indus.veena.database.sqlite.entities.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {
    @Query("SELECT query FROM search_history ORDER BY timestamp DESC")
    fun getHistoryQueries(): Flow<List<String>>

    @Query("SELECT * FROM search_history")
    suspend fun getAllHistoryList(): List<SearchHistoryEntity>

    @Insert
    suspend fun insertHistory(item: SearchHistoryEntity)

    @Insert
    suspend fun insertHistoryList(items: List<SearchHistoryEntity>)

    @Query("DELETE FROM search_history WHERE query = :query")
    suspend fun deleteHistoryItem(query: String)

    @Query("DELETE FROM search_history")
    suspend fun clearHistory()
}