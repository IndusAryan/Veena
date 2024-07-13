package com.aryan.veena.repository

import com.aryan.veena.repository.saavn.SaavnProvider
import com.aryan.veena.repository.newpipe.NewPipeProvider
import com.aryan.veena.repository.piped.PipedProvider
import com.aryan.veena.repository.wapking.WapKingProvider
import com.aryan.veena.repository.ytmusic.YTMusicProvider

enum class Provider(val musicProvider: MusicProvider<*>) {
    SAAVN(SaavnProvider()),
    YTMUSIC(YTMusicProvider()),
    NEWPIPE(NewPipeProvider()),
    PIPED(PipedProvider()),
    WAPKING(WapKingProvider()),
}