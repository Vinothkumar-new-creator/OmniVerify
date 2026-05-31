package com.omniverify

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface OmniApi {
    // For Image
    @Multipart
    @POST("verify")
    fun verifyImage(@Part file: MultipartBody.Part): Call<VerifyResponse>

    // For Text
    @POST("verify-text")
    fun verifyText(@Body body: Map<String, String>): Call<VerifyResponse>

    // For Link
    @POST("verify-link")
    fun verifyLink(@Body body: Map<String, String>): Call<VerifyResponse>
}
