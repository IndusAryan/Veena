package com.indus.veena.di

import android.content.Context
import com.indus.veena.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkingModule {

    @Provides
    @Singleton
    fun provideCache(@ApplicationContext context: Context): Cache {
        return Cache(File(context.cacheDir, "http_cache"), 10 * 1024 * 1024)
    }

    @Provides
    @Singleton
    fun provideRetryInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            var response = chain.proceed(request)
            var attempt = 0
            while (response.code in setOf(408, 429, 500, 502, 503, 504) && attempt < 2) {
                attempt++
                val backoff = attempt * 1000L
                response.close()
                try {
                    Thread.sleep(backoff)
                } catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
                response = chain.proceed(request)
            }
            response
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(cache: Cache, retryInterceptor: Interceptor): OkHttpClient {
        val builder = OkHttpClient.Builder().cache(cache).connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS).addInterceptor(retryInterceptor)
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS))
        }
        return builder.build()
    }
}