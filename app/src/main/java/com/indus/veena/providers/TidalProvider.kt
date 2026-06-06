package com.indus.veena.providers

import android.util.Base64
import android.util.Log
import com.indus.veena.models.AlbumModel
import com.indus.veena.models.ArtistModel
import com.indus.veena.models.Provider
import com.indus.veena.models.SongModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class TidalProvider(
    private val client: OkHttpClient
) : MainMusicProvider() {

    private val TAG = "VEENA_TIDAL"
    override val providerName = Provider.Tidal
    override val baseUrl = "https://tidal.com"
    private val apiBaseUrl = "https://api.tidalhifi.com/v1"
    private val authUrl = "https://auth.tidal.com/v1/oauth2/token"

    // Decoded from the Python script's Base64 strings
    private val clientId = String(Base64.decode("ZlgySnhkbW50WldLMGl4VA==", Base64.DEFAULT))
    private val clientSecret = String(Base64.decode("MU5tNUFmREFqeHJnSkZKYktOV0xlQXlLR1ZHbUlOdVhQUExIVlhBdnhBZz0=", Base64.DEFAULT))

    private var accessToken: String? = null
    private val defaultCountryCode = "US" // Tidal strictly requires a country code

    /**
     * STAGE 1: SEARCH
     */
    override suspend fun searchSongs(query: String, page: Int): List<SongModel> = withContext(Dispatchers.IO) {
        try {
            ensureToken()
            Log.d(TAG, "🔍 Starting Tidal Search: $query")

            val url = "$apiBaseUrl/search/tracks?query=$query&limit=50&countryCode=$defaultCountryCode"

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $accessToken")
                .build()

            val response = client.newCall(request).execute().use { it.body?.string() ?: "{}" }
            val items = JSONObject(response).optJSONObject("tracks")?.optJSONArray("items")

            val songs = mutableListOf<SongModel>()
            if (items != null) {
                for (i in 0 until items.length()) {
                    val track = items.getJSONObject(i)
                    val id = track.optString("id")
                    val title = track.optString("title", "Unknown")
                    val artist = track.optJSONArray("artists")?.optJSONObject(0)?.optString("name", "Unknown Artist") ?: "Unknown"
                    val duration = track.optLong("duration", 0).toString() // Tidal returns seconds usually

                    // Tidal image URLs require string replacement: {w}x{h}
                    val rawCover = track.optJSONObject("album")?.optString("cover") ?: ""
                    val coverUrl = if (rawCover.isNotEmpty()) {
                        "https://resources.tidal.com/images/${rawCover.replace("-", "/")}/640x640.jpg"
                    } else ""

                    songs.add(
                        SongModel(
                            id = id,
                            title = title,
                            artist = artist,
                            thumbnail = coverUrl,
                            duration = duration,
                            provider = providerName.name,
                            composer = "", genre = "", year = ""
                        )
                    )
                }
            }
            return@withContext songs
        } catch (e: Exception) {
            Log.e(TAG, "❌ Search failed", e)
            emptyList()
        }
    }

    /**
     * STAGE 2: DETAILS & STREAM FETCH
     */
    override suspend fun getSongDetails(songId: String): SongModel = withContext(Dispatchers.IO) {
        ensureToken()
        Log.d(TAG, "📋 Fetching Details from Tidal for: $songId")

        // 1. We first fetch the track details (metadata)
        val trackUrl = "$apiBaseUrl/tracks/$songId?countryCode=$defaultCountryCode"
        val trackRequest = Request.Builder().url(trackUrl).addHeader("Authorization", "Bearer $accessToken").build()
        val trackResponse = client.newCall(trackRequest).execute().use { it.body?.string() ?: "{}" }
        val trackObj = JSONObject(trackResponse)

        val title = trackObj.optString("title", "Unknown")
        val artist = trackObj.optJSONArray("artists")?.optJSONObject(0)?.optString("name", "Unknown Artist") ?: "Unknown"
        val duration = trackObj.optLong("duration", 0).toString()
        val rawCover = trackObj.optJSONObject("album")?.optString("cover") ?: ""
        val coverUrl = if (rawCover.isNotEmpty()) "https://resources.tidal.com/images/${rawCover.replace("-", "/")}/640x640.jpg" else ""

        // 2. Fetch the playable streams using PlaybackInfo
        val playableURLs = mutableMapOf<String, String>()

        // Tidal Qualities: LOW (AAC 96), HIGH (AAC 320), LOSSLESS (FLAC 16/44.1), HI_RES (MQA/FLAC 24/96)
        val qualities = listOf("LOW", "HIGH", "LOSSLESS")

        for (quality in qualities) {
            try {
                val playbackUrl = "$apiBaseUrl/tracks/$songId/playbackinfopostpaywall" +
                        "?audioquality=$quality&playbackmode=STREAM&assetpresentation=FULL&countryCode=$defaultCountryCode"

                val pbRequest = Request.Builder()
                    .url(playbackUrl)
                    .addHeader("Authorization", "Bearer $accessToken")
                    .build()

                val pbResponse = client.newCall(pbRequest).execute()

                if (pbResponse.isSuccessful) {
                    val pbObj = JSONObject(pbResponse.body?.string() ?: "{}")
                    val manifestBase64 = pbObj.optString("manifest")

                    if (manifestBase64.isNotEmpty()) {
                        // The manifest is Base64 encoded JSON, we decode it as per Python logic
                        val manifestStr = String(Base64.decode(manifestBase64, Base64.DEFAULT))
                        val manifestObj = JSONObject(manifestStr)

                        val urlsArray = manifestObj.optJSONArray("urls")
                        if (urlsArray != null && urlsArray.length() > 0) {
                            val streamUrl = urlsArray.getString(0)

                            // Map to your app's standard bitrate keys
                            val mappedQuality = when (quality) {
                                "LOSSLESS" -> "320kbps" // Technically higher (FLAC), but mapped to your top tier
                                "HIGH" -> "256kbps"     // Tidal HIGH is 320 AAC, but standardizing naming
                                else -> "128kbps"       // LOW is usually AAC 96
                            }
                            playableURLs[mappedQuality] = streamUrl
                        }
                    }
                } else {
                    Log.w(TAG, "Tidal rejected quality $quality. (Needs Premium?)")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to get $quality stream: ${e.message}")
            }
        }

        Log.d(TAG, "📡 Stream URLs found: ${playableURLs.keys}")

        return@withContext SongModel(
            id = songId,
            title = title,
            artist = artist,
            thumbnail = coverUrl,
            duration = duration,
            streamableUrls = playableURLs,
            provider = providerName.name,
            genre = "", composer = "", year = ""
        )
    }

    /**
     * Fetches a Client Credentials token.
     * Note: This works for searching. If Tidal blocks `playbackinfopostpaywall`
     * without a premium user, you will need to implement User Login.
     */
    private suspend fun ensureToken() {
        if (!accessToken.isNullOrEmpty()) return

        Log.d(TAG, "🔑 Fetching Tidal App Token...")

        val body = FormBody.Builder()
            .add("grant_type", "client_credentials")
            .build()

        val credential = Credentials.basic(clientId, clientSecret)

        val request = Request.Builder()
            .url(authUrl)
            .addHeader("Authorization", credential)
            .post(body)
            .build()

        val response = client.newCall(request).execute().use {
            if (!it.isSuccessful) throw IOException("Failed to get Tidal Token: ${it.code}")
            it.body?.string() ?: ""
        }

        accessToken = JSONObject(response).optString("access_token")
        Log.d(TAG, "✅ Tidal Token Retrieved!")
    }

    // Unused internal stubs
    override suspend fun searchAlbums(query: String, page: Int) = emptyList<AlbumModel>()
    override suspend fun searchArtists(query: String, page: Int) = emptyList<ArtistModel>()
    override suspend fun getAlbumDetails(albumId: String) = null
}