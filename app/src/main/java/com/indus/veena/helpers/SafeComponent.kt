package com.indus.veena.helpers

import android.util.Log
import com.indus.veena.BuildConfig
import com.indus.veena.models.AlbumModel
import com.indus.veena.models.SongModel
import kotlinx.serialization.json.Json

// utils/VeenaLog.kt
object VeenaLog {
    // Only log if the build is a Debug build
    private val isDebug = BuildConfig.DEBUG
    private const val GLOBAL_TAG = "VEENA"
    fun d(tag: String, message: String) {
        if (isDebug) Log.d("$GLOBAL_TAG:$tag", message)
    }

    fun e(tag: String, message: String, e: Throwable? = null) {
        if (isDebug) Log.e("$GLOBAL_TAG:$tag", message, e)
    }

    fun Any?.asAutoJson(): String {
        if (this == null) return "null"
        return try {
            val fields = this.javaClass.declaredFields
            val builder = StringBuilder("{\n")
            fields.forEachIndexed { index, field ->
                field.isAccessible = true
                val value = field.get(this)
                builder.append("  \"${field.name}\": ")
                if (value is Map<*, *>) builder.append(value.keys.toString()) // Keep maps clean
                else builder.append("\"$value\"")

                if (index < fields.size - 1) builder.append(",")
                builder.append("\n")
            }
            builder.append("}").toString()
        } catch (e: Exception) {
            this.toString() // Fallback to standard toString if reflection fails
        }
    }

    fun SongModel.toLogString() = """
    {
      "id": "$id",
      "title": "$title",
      "artist": "$artist",
      "duration": "$duration",
      "provider": "$provider",
      "hasUrls": ${streamableUrls.isNotEmpty()},
      "qualities": [${streamableUrls.keys.joinToString()}]
    }
""".trimIndent()

    fun AlbumModel.toLogString() = """
    {
      "id": "$id",
      "title": "$title",
      "artist": "$artist",
      "cover": "$coverUrl"
    }
""".trimIndent()
}