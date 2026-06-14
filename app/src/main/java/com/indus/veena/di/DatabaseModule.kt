package com.indus.veena.di

import android.content.Context
import androidx.room3.Room
import com.indus.veena.R
import com.indus.veena.database.sqlite.VeenaDB
import com.indus.veena.database.sqlite.VeenaDB.Companion.MIGRATION_1_2
import com.indus.veena.database.sqlite.VeenaDB.Companion.MIGRATION_2_3
import com.indus.veena.database.sqlite.daos.DownloadDao
import com.indus.veena.database.sqlite.daos.FavouriteDao
import com.indus.veena.database.sqlite.daos.SearchHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): VeenaDB {
        return Room.databaseBuilder(
            context,
            VeenaDB::class.java,
            "${context.getString(R.string.app_name)}_db"
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()
    }

    @Provides
    @Singleton
    fun provideDownloadDao(database: VeenaDB): DownloadDao { return database.downloadDao() }

    @Provides
    @Singleton
    fun provideSearchHistoryDao(database: VeenaDB): SearchHistoryDao { return database.searchHistoryDao() }

    @Provides
    @Singleton
    fun provideFavouriteDao(database: VeenaDB): FavouriteDao { return database.favouriteDao() }
}