package com.aryan.veena.repository

abstract class MusicProvider<T> {

    // Provider name, from repository/Provider enum class
    open var provider : Provider? = null

    abstract suspend fun search(query: String?): List<T>
    // Search the query parameter in api , it should return a list of responses,
    // mostly the result data class of that api

    abstract suspend fun getSong(id: String?): String?
    // Only needed when an api or source doesn't provide a direct streamable url in a single call
    // so with metadata do stuff here and return a streamable url in string format
    // this function is only invoked when url in player is null and provider and songID is given

    abstract fun mapPlayerData(item: T?): NowPlayingModel
    // This is where the api or source response will be mapped to the NowPlayingModel data class
    // after fetching stuff, map it like this
    /** return NowPlayingModel(
        name = item.name,
        etc
     )
    **/
}