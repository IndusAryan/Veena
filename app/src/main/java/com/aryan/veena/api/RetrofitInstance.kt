package com.aryan.veena.api

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

object RetrofitInstance {
    private const val SAAVN_URL = "https://saavn.dev"
    private const val PIPED_URL = "https://pipedapi.kavin.rocks"
    private val kotlinxConverterFactory = JsonHelper.json.asConverterFactory("application/json".toMediaType())

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val saavnAPI : Retrofit = Retrofit.Builder()
        .baseUrl(SAAVN_URL)
        .client(client)
        .addConverterFactory(kotlinxConverterFactory)
        .build()

    val pipedAPI : Retrofit = Retrofit.Builder()
        .baseUrl(PIPED_URL)
        .addConverterFactory(kotlinxConverterFactory)
        .build()
}