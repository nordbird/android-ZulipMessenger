package ru.nordbird.tfsmessenger.data.api

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import ru.nordbird.tfsmessenger.data.api.ZulipAuth.AUTH_EMAIL
import ru.nordbird.tfsmessenger.data.api.ZulipAuth.AUTH_KEY

object ZulipServiceImpl {
    private const val BASE_URL = "https://tfs-android-2021-spring.zulipchat.com/api/v1/"

    private val contentType = MediaType.get("application/json")

    private val client = OkHttpClient.Builder()
        .addInterceptor(HeaderInterceptor(AUTH_EMAIL, AUTH_KEY))
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .client(client)
        .baseUrl(BASE_URL)
        .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory(contentType))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()


    private val zulipService = retrofit.create(ZulipService::class.java)

    fun getApi(): ZulipService = zulipService
}