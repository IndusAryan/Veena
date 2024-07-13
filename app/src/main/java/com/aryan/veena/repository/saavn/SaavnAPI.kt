package com.aryan.veena.repository.saavn


import com.aryan.veena.api.RetrofitInstance.saavnAPI
import retrofit2.http.GET
import retrofit2.http.Query

object SaavnAPI {
     val fetchSaavnAPI: SaavnAPIService = saavnAPI.create(SaavnAPIService::class.java)
}

interface SaavnAPIService {
    @GET("/api/search/songs")
    suspend fun getSongs(@Query("query") query:String): Root
}