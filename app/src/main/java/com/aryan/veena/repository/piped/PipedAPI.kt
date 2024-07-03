package com.aryan.veena.repository.piped

import com.aryan.veena.api.RetrofitInstance.pipedAPI

object PipedAPI {
    val pipedService : PipedAPIService by lazy {
       pipedAPI.create(PipedAPIService::class.java)
    }

}