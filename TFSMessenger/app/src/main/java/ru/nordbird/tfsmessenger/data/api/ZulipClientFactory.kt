package ru.nordbird.tfsmessenger.data.api

import okhttp3.OkHttpClient
import ru.nordbird.tfsmessenger.data.api.ZulipAuth.AUTH_EMAIL
import ru.nordbird.tfsmessenger.data.api.ZulipAuth.AUTH_KEY
import java.util.concurrent.TimeUnit

object ZulipClientFactory {

    private const val TIMEOUT: Long = 60

    fun create(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HeaderInterceptor(AUTH_EMAIL, AUTH_KEY))
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

}