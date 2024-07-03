package com.aryan.veena.repository.piped

import retrofit2.http.GET
import retrofit2.http.Path

interface PipedAPIService {
    @GET("streams/{videoId}")
    suspend fun getStreamURL(@Path("videoId") videoId : String) : PipedModel
}