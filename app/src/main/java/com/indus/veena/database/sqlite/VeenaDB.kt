package com.indus.veena.database.sqlite

import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.execSQL
import com.indus.veena.database.sqlite.daos.DownloadDao
import com.indus.veena.database.sqlite.daos.SearchHistoryDao
import com.indus.veena.database.sqlite.entities.DownloadEntity
import com.indus.veena.database.sqlite.entities.SearchHistoryEntity

@Database(
    entities = [DownloadEntity::class, SearchHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class VeenaDB : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
    abstract fun searchHistoryDao(): SearchHistoryDao
}