package com.aryan.veena.helpers

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.aryan.veena.R
import com.aryan.veena.utils.CoroutineUtils.ioScope
import com.aryan.veena.utils.ToastUtil.showToast
import kotlinx.coroutines.cancel
import java.io.File
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

object DownloadHelper {

    fun downloadFile(
        context: Context, fileUrl: String, fileName: String, artistName: String,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            showToast(context, R.string.downloading)
            ioScope {
                try {
                    val url = URL(fileUrl)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connect()

                    if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                        throw Exception("Server returned HTTP ${connection.responseCode} ${connection.responseMessage}")
                    }

                    val inputStream = connection.inputStream
                    //val fileLength = connection.contentLength
                    val values = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.ARTIST, artistName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            put(
                                MediaStore.MediaColumns.RELATIVE_PATH,
                                Environment.DIRECTORY_DOWNLOADS + File.separator + "Veena"
                            )
                        }
                    }

                    val uri: Uri? = context.contentResolver.insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI, values
                    )

                    uri?.let { it ->
                        val outputStream: OutputStream? =
                            context.contentResolver.openOutputStream(it)
                        outputStream?.use { stream ->
                            val buffer = ByteArray(4096)
                            var totalBytesRead: Long = 0
                            var bytesRead: Int
                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                stream.write(buffer, 0, bytesRead)
                                totalBytesRead += bytesRead
                            }
                        }
                    }

                    inputStream.close()
                    connection.disconnect()
                    showToast(context, R.string.download_successful)

                } catch (t: Throwable) {
                    cancel()
                    Log.e("DownloadHelper", "Error downloading", t)
                    showToast(context, R.string.download_error)
                }
            }

        } else {
            // TODO, DOWNLOADING FOR DEVICES < OREO
        }
    }
}