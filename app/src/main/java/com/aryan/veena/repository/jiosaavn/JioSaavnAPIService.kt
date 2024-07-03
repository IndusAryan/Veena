package com.aryan.veena.repository.jiosaavn

import retrofit2.http.GET
import retrofit2.http.Query

interface JioSaavnAPIService {
    @GET("/api/search/songs")
    suspend fun getSongs(@Query("query") query:String): Root
}