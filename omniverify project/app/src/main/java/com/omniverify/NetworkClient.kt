package com.omniverify

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {
    private const val BASE_URL = "https://omniverify.onrender.com/"

    // Must match APP_SHARED_SECRET on the Render backend (main.py).
    // NOTE: this is a basic abuse deterrent, not real security -- anyone who
    // decompiles the APK can read this string. It stops casual scraping of
    // your Sightengine/Gemini quota; it does not stop a determined attacker.
    private const val APP_SHARED_SECRET = "CHARLIEISTHEBEST"

    private val authInterceptor = Interceptor { chain ->
        val requestWithKey = chain.request().newBuilder()
            .addHeader("X-OmniVerify-Key", APP_SHARED_SECRET)
            .build()
        chain.proceed(requestWithKey)
    }

    // Generous enough to survive a slow mobile connection, but short enough
    // that a genuinely broken request fails fast instead of hanging for a
    // full minute. Paired with the warm-up ping below, real requests should
    // complete in a few seconds once the backend is awake.
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Separate client just for the warm-up ping: it's fire-and-forget and is
    // allowed to take longer (covers a cold Render free-tier instance
    // waking up), but it must NEVER block anything the user is waiting on.
    private val warmUpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .build()

    val api: OmniApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OmniApi::class.java)
    }

    /**
     * Fire-and-forget ping to /health. Call this the moment the floating
     * assistant turns on (or the app opens) -- NOT right before a scan.
     * If the backend is on a free tier that sleeps after inactivity, this
     * gives it time to wake up in the background while the user is still
     * looking for something to crop, instead of making the actual scan
     * request eat that cold-start delay.
     */
    fun warmUp() {
        val request = Request.Builder()
            .url(BASE_URL + "health")
            .get()
            .build()
        warmUpClient.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                // Safe to ignore -- this is just a best-effort warm-up.
            }
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.close()
            }
        })
    }
}
