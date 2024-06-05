package com.aryan.veena.interfaces

import com.aryan.veena.datamodels.Root
import retrofit2.http.GET
import retrofit2.http.Query

interface JioSaavnAPIService {
    @GET("/api/search/songs")
    suspend fun getSongs(@Query("query") query:String): Root
}