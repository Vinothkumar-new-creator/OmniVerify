package com.omniverify

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkClient {
    // Update this to your computer's current IPv4 Address (Type 'ipconfig' in CMD)
    private const val BASE_URL = "http://10.18.184.170:8000/"

    val api: OmniApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OmniApi::class.java)
    }
}
