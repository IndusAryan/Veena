package com.indus.veena.database.sqlite.daos

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.indus.veena.database.sqlite.entities.FavouriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteDao {
    @Query("SELECT * FROM favourites")
    fun getAllFavourites(): Flow<List<FavouriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favourites WHERE songId = :songId)")
    fun isFavourite(songId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavourite(favourite: FavouriteEntity)

    @Delete
    suspend fun deleteFavourite(favourite: FavouriteEntity)

    @Query("DELETE FROM favourites WHERE songId = :songId")
    suspend fun deleteBySongId(songId: String)
}