package com.indus.veena.database.sqlite

import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.indus.veena.database.sqlite.daos.DownloadDao
import com.indus.veena.database.sqlite.daos.FavouriteDao
import com.indus.veena.database.sqlite.daos.SearchHistoryDao
import com.indus.veena.database.sqlite.entities.DownloadEntity
import com.indus.veena.database.sqlite.entities.FavouriteEntity
import com.indus.veena.database.sqlite.entities.SearchHistoryEntity

@Database(
    entities = [DownloadEntity::class, SearchHistoryEntity::class, FavouriteEntity::class],
    version = 3,
    exportSchema = false
)
abstract class VeenaDB : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun favouriteDao(): FavouriteDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override suspend fun migrate(connection: SQLiteConnection) {
                connection.execSQL("CREATE TABLE IF NOT EXISTS `search_history_new` (`query` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, PRIMARY KEY(`query`))")
                connection.execSQL("INSERT OR REPLACE INTO `search_history_new` (query, timestamp) SELECT query, timestamp FROM search_history")
                connection.execSQL("DROP TABLE search_history")
                connection.execSQL("ALTER TABLE search_history_new RENAME TO search_history")
                connection.execSQL("CREATE TABLE IF NOT EXISTS `favourites` (`songId` TEXT NOT NULL, `title` TEXT NOT NULL, `artist` TEXT NOT NULL, `thumbnail` TEXT NOT NULL, `url` TEXT NOT NULL, `duration` TEXT NOT NULL, `provider` TEXT NOT NULL, `album` TEXT NOT NULL, `year` TEXT NOT NULL, `composer` TEXT NOT NULL, `genre` TEXT NOT NULL, PRIMARY KEY(`songId`))")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override suspend fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE `favourites` ADD COLUMN `extensionName` TEXT NOT NULL DEFAULT ''")
                connection.execSQL("ALTER TABLE `downloads` ADD COLUMN `extensionName` TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}