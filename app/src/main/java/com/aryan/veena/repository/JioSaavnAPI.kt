package com.aryan.veena.repository

import com.aryan.veena.api.RetrofitInstance.api
import com.aryan.veena.interfaces.JioSaavnAPIService

object JioSaavnAPI {
    val retrofitService: JioSaavnAPIService by lazy {
        api.create(JioSaavnAPIService::class.java)
    }
}