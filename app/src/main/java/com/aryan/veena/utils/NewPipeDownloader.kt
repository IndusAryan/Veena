package com.aryan.veena.utils

import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import java.util.concurrent.TimeUnit

class NewPipeDownloader private constructor(builder: OkHttpClient.Builder) : Downloader() {
    private val client: OkHttpClient = builder.readTimeout(15, TimeUnit.SECONDS).build()

    override fun execute(request: Request): Response {
        val httpMethod = request.httpMethod()
        val url = request.url()
        val headers = request.headers()
        val dataToSend = request.dataToSend()
        var requestBody: RequestBody? = null
        if (dataToSend != null) {
            requestBody = dataToSend.toRequestBody(null, 0, dataToSend.size)
        }
        val requestBuilder: okhttp3.Request.Builder = okhttp3.Request.Builder().method(httpMethod, requestBody).url(url).addHeader("User-Agent", USER_AGENT)
        for ((headerName, headerValueList) in headers) {
            if (headerValueList.size > 1) {
                requestBuilder.removeHeader(headerName)
                for (headerValue in headerValueList) {
                    requestBuilder.addHeader(headerName, headerValue)
                }
            } else if (headerValueList.size == 1) {
                requestBuilder.header(headerName, headerValueList[0])
            }
        }
        val response: okhttp3.Response = client.newCall(requestBuilder.build()).execute()
        if (response.code == 429) {
            response.close()
            throw ReCaptchaException("reCaptcha Challenge requested", url)
        }
        val body: ResponseBody = response.body
        val responseBodyToReturn: String = body.string()
        val latestUrl: String = response.request.url.toString()
        return Response(
            response.code, response.message, response.headers.toMultimap(), responseBodyToReturn, latestUrl
        )
    }

    companion object {
        const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; rv:91.0) Gecko/20100101 Firefox/91.0"
        private var instance: Downloader? = null

        /**
         * It's recommended to call exactly once in the entire lifetime of the application.
         *
         * builder if null, default builder will be used
         * @return a new instance of DownloaderTestImpl
         */
        private fun init(): Downloader? {
            instance = NewPipeDownloader(
                 OkHttpClient.Builder()
            )
            return instance
        }

        fun getInstance(): Downloader? {
            if (instance == null) {
                init()
            }
            return instance
        }
    }
}