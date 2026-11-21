package com.indus.veena.models

/** Signed transports may rotate without invalidating the artwork/credits currently on screen. */
internal fun SongModel.retainUnchangedPresentation(previous: SongModel): SongModel =
    if (copy(streamableUrls = previous.streamableUrls, playbackHeaders = previous.playbackHeaders,
            downloadHeaders = previous.downloadHeaders, url = previous.url) == previous) previous else this
