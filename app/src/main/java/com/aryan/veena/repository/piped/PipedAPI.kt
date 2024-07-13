package com.aryan.veena.repository.piped

import com.aryan.veena.api.RetrofitInstance.pipedAPI
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

object PipedAPI {
    val fetchPipedAPI : PipedAPIService = pipedAPI.create(PipedAPIService::class.java)
}

interface PipedAPIService {
    @GET("streams/{videoId}")
    suspend fun getStreamURL(@Path("videoId") videoId : String) : PipedModel
    @GET("search")
    suspend fun getSearchResults(
        @Query("q") searchQuery: String, @Query("filter") filter: String
    ): SearchRoot
}