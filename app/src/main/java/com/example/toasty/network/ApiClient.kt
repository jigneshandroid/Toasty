package com.jigs.chatgptdemo.network

import com.example.toasty.interfaces.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://dummy-json.mock.beeceptor.com/"
    private const val BASE_URL_STAGING_MAIN = "https://staging-main-www.zoodmall.com/interface/"
    //private const val BASE_URL = "https://api.openai.com/"

    val chatApi: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    val retrofitApi: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_STAGING_MAIN)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
