package com.indus.veena.di

import android.util.Log
import com.indus.veena.contract.ExtensionHost
import com.indus.veena.contract.HttpResponse
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExtensionModule {

    @Provides
    @Singleton
    fun provideExtensionHost(okHttpClient: OkHttpClient): ExtensionHost {
        val hostScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        return object : ExtensionHost {

            private fun buildBaseRequest(url: String, headers: Map<String, String>): Request.Builder {
                return Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .apply {
                        headers.forEach { (k, v) -> addHeader(k, v) }
                    }
            }

            private fun okResponseToHttpResponse(response: okhttp3.Response, body: String): HttpResponse {
                return HttpResponse(
                    code = response.code,
                    headers = response.headers.toMultimap(),
                    body = body,
                    finalUrl = response.request.url.toString()
                )
            }

            override fun httpGet(url: String, headers: Map<String, String>): String =
                httpGetFull(url, headers).body

            override fun httpPost(url: String, body: String, contentType: String, headers: Map<String, String>): String =
                httpPostFull(url, body, contentType, headers).body

            override fun httpGetFull(url: String, headers: Map<String, String>): HttpResponse {
                val request = buildBaseRequest(url, headers).get().build()
                okHttpClient.newCall(request).execute().use { response ->
                    val body = response.body.string()
                    if (!response.isSuccessful) {
                        Log.e("ExtensionHost", "GET failed [${response.code}] url=$url")
                    }
                    return okResponseToHttpResponse(response, body)
                }
            }

            override fun httpPostFull(url: String, body: String, contentType: String, headers: Map<String, String>): HttpResponse {
                val requestBody = body.toRequestBody(contentType.toMediaTypeOrNull())
                val request = buildBaseRequest(url, headers).post(requestBody).build()
                okHttpClient.newCall(request).execute().use { response ->
                    val responseBody = response.body.string()
                    if (!response.isSuccessful) {
                        Log.e("ExtensionHost", "POST failed [${response.code}] url=$url")
                    }
                    return okResponseToHttpResponse(response, responseBody)
                }
            }

            override fun decrypt(payload: String, extractor: String): String {
                return when (extractor.lowercase()) {
                    "foo" -> ""
                    else -> "{}"
                }
            }

            override fun storageGet(extensionId: String, key: String): String? = null
            override fun storageSet(extensionId: String, key: String, value: String) {}
            override fun storageRemove(extensionId: String, key: String) {}

            override fun <T> runSuspending(block: suspend () -> T): T {
                return runBlocking(hostScope.coroutineContext) { block() }
            }

            override suspend fun <T> runOnIO(block: suspend () -> T): T {
                return withContext(hostScope.coroutineContext) { block() }
            }

            override suspend fun <T> runOnMain(block: suspend () -> T): T {
                return withContext(Dispatchers.Main) { block() }
            }
        }
    }
}