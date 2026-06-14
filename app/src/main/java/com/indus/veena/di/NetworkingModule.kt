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
            while (!response.isSuccessful && attempt < 3) {
                attempt++
                val backoff = attempt * 1000L
                try {
                    Thread.sleep(backoff)
                } catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
                response.close()
                response = chain.proceed(request)
            }
            response
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(cache: Cache, retryInterceptor: Interceptor): OkHttpClient {
        val builder = OkHttpClient.Builder().cache(cache).connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS).addInterceptor { chain ->
                var request = chain.request()
                request = request.newBuilder().header(
                    "Cache-Control", "public, max-age=" + 5
                ).build()
                chain.proceed(request)
            }.addInterceptor(retryInterceptor)
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        }
        return builder.build()
    }
}