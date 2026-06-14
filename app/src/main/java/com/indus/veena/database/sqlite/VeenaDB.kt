package com.indus.veena.database.sqlite

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.indus.veena.database.sqlite.daos.DownloadDao
import com.indus.veena.database.sqlite.daos.FavouriteDao
import com.indus.veena.database.sqlite.daos.SearchHistoryDao
import com.indus.veena.database.sqlite.entities.DownloadEntity
import com.indus.veena.database.sqlite.entities.FavouriteEntity
import com.indus.veena.database.sqlite.entities.SearchHistoryEntity

@Database(
    entities = [DownloadEntity::class, SearchHistoryEntity::class, FavouriteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class VeenaDB : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun favouriteDao(): FavouriteDao

    /*companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override suspend fun migrate(connection: SQLiteConnection) {

            }
        }
    }*/
}