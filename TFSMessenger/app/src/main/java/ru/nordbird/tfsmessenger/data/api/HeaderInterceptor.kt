package ru.nordbird.tfsmessenger.data.api

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Request

class HeaderInterceptor(
    email: String,
    apiKey: String
) : Interceptor {

    private val credential: String = Credentials.basic(email, apiKey)

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request: Request = chain.request()
        val authenticatedRequest: Request = request.newBuilder()
            .header("Authorization", credential)
            .build()
        return chain.proceed(authenticatedRequest)
    }
}