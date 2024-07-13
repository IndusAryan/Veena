package com.aryan.veena.api

import com.aryan.veena.repository.wapking.WapKingModel
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create
import retrofit2.http.GET

object RetrofitInstance {
    private const val SAAVN_URL = "https://saavn.dev"
    private const val PIPED_URL = "https://pipedapi.kavin.rocks"
    const val WAPKING_URL = "https://wapking.name"
    const val USER_AGENT = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.6478.122 Mobile Safari/537.36"

    private val kotlinxConverterFactory = JsonHelper.json.asConverterFactory("application/json".toMediaType())

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("User-Agent", USER_AGENT)
                .build()
            chain.proceed(request)
        }
        .build()

    val saavnAPI : Retrofit = Retrofit.Builder()
        .baseUrl(SAAVN_URL)
        .client(okHttpClient)
        .addConverterFactory(kotlinxConverterFactory)
        .build()

    val pipedAPI : Retrofit = Retrofit.Builder()
        .baseUrl(PIPED_URL)
        .client(okHttpClient)
        .addConverterFactory(kotlinxConverterFactory)
        .build()

    //fun scrape(url : String) {
        val scrapeWapking: Retrofit = Retrofit.Builder()
            .baseUrl(WAPKING_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()

    //}
}