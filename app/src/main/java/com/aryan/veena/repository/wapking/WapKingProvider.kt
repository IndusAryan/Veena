package com.aryan.veena.repository.wapking

import com.aryan.veena.api.RetrofitInstance
import com.aryan.veena.api.RetrofitInstance.scrapeWapking
import com.aryan.veena.repository.MusicProvider
import com.aryan.veena.repository.NowPlayingModel
import com.aryan.veena.repository.Provider
import com.lagradost.nicehttp.Requests
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface Scrape {
    @GET("files-search/{query}/new2old/1.html")
    suspend fun scrapeSearchResults(@Path ("query") searchQuery : String ) : String
}

class WapKingProvider : MusicProvider<WapKingModel>() {

    //val requests = Requests()
    private val scrapeService = scrapeWapking.create(Scrape::class.java)
    init {
        provider = Provider.WAPKING
    }

    override suspend fun search(query: String?): List<WapKingModel> {
        val htmlResponse = scrapeService.scrapeSearchResults(query!!)
        val document = Jsoup.parse(htmlResponse)
       // val document = requests.get("$BASE_URL/files-search/$query/new2old/1.html").document
        val results = document.select("div.fl").mapNotNull { it.toWapKingModel() }

        println(results)
        return results
    }

    private fun Element.toWapKingModel(): WapKingModel {
        val title = this.select("a.fileName div div").text().split("Singer:")[0].trim()
        // song page url (for fetching stream)
        val href = this.select("a.fileName").attr("href")
        // thumbnail
        val posterUrl = this.select("img.absmiddle").attr("src")
        // artist names
        val artistName = this.select("span.ar").joinToString(", ") { it.text() }
        // size to show in place of duration
        val size = this.select("span.ar").parents().select("span").last()?.text()

        return WapKingModel(title, href, posterUrl, artistName, size)
    }

    override suspend fun getSong(id: String?): String? {
        val songPageDocument = Jsoup.connect(id.toString()).get()
        //val songPageDocument = requests.get("$id").document
        val downloadLinks = songPageDocument.select("a.dbutton")
            .filter { element -> element.attr("href").contains("download") }
            .mapNotNull { element ->  element.attr("href") to element.attr("title") }

        var streamURL128Kbps: String? = null
        var streamURL320Kbps: String? = null
        var fallbackURL: String? = null

        downloadLinks.forEach { (href, title) ->
            when {
                title.contains("128 Kbps", ignoreCase = true) -> streamURL128Kbps = href
                title.contains("320 Kbps", ignoreCase = true) -> streamURL320Kbps = href
                fallbackURL == null -> fallbackURL = href // Set the first available link as fallback
            }
        }

        println(downloadLinks)
        // Prioritize returning 320 Kbps URL if available, otherwise return 128 Kbps URL
        return streamURL128Kbps ?: streamURL320Kbps ?: fallbackURL
    }

    override fun mapPlayerData(item: WapKingModel?): NowPlayingModel {

        return NowPlayingModel(
            id = item?.href,
            url = null,
            name = item?.title,
            artistName = item?.artists,
            duration = null,
            imageUrl = item?.posterUrl,
            size = item?.size,
            provider = Provider.WAPKING,
        )
    }

    /*companion object {
        const val BASE_URL = "https://wapking.name"
    }*/
}