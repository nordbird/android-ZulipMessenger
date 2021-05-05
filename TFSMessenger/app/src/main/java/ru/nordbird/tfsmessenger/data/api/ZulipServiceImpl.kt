package ru.nordbird.tfsmessenger.data.api

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import ru.nordbird.tfsmessenger.data.api.ZulipAuth.AUTH_EMAIL
import ru.nordbird.tfsmessenger.data.api.ZulipAuth.AUTH_KEY
import ru.nordbird.tfsmessenger.data.api.ZulipConst.BASE_URL
import java.util.concurrent.TimeUnit

object ZulipServiceImpl {
    private const val BASE_URL_API = "$BASE_URL/api/v1/"
    private const val TIMEOUT: Long = 60

    private val client = OkHttpClient.Builder()
        .addInterceptor(HeaderInterceptor(AUTH_EMAIL, AUTH_KEY))
        .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .client(client)
        .baseUrl(BASE_URL_API)
        .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory(MediaType.get("application/json")))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()

    private val zulipService = retrofit.create(ZulipService::class.java)

    fun getApi(): ZulipService = zulipService
}