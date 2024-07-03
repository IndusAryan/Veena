package com.aryan.veena.repository.jiosaavn

import com.aryan.veena.api.RetrofitInstance.saavnAPI

object JioSaavnAPI {
    val retrofitService: JioSaavnAPIService by lazy {
        saavnAPI.create(JioSaavnAPIService::class.java)
    }
}