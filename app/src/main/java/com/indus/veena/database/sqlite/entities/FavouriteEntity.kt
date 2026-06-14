package com.indus.veena.database.sqlite.entities

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "favourites")
data class FavouriteEntity(
    @PrimaryKey val songId: String,
    val title: String,
    val artist: String,
    val thumbnail: String,
    val url: String,
    val duration: String,
    val provider: String,
    val extensionName: String = "",
    val album: String = "",
    val year: String = "",
    val composer: String = "",
    val genre: String = ""
)