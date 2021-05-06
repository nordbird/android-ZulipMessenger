package ru.nordbird.tfsmessenger.data.api

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import ru.nordbird.tfsmessenger.data.api.ZulipConst.BASE_URL

object ZulipServiceFactory {

    private const val BASE_URL_API = "$BASE_URL/api/v1/"

    fun create(client: OkHttpClient): ZulipService {
        return Retrofit.Builder()
            .client(client)
            .baseUrl(BASE_URL_API)
            .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory(MediaType.get("application/json")))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(ZulipService::class.java)
    }
}