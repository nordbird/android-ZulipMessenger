package ru.nordbird.tfsmessenger.data.api

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import ru.nordbird.tfsmessenger.data.model.PresenceResponse
import ru.nordbird.tfsmessenger.data.model.UserResponse
import ru.nordbird.tfsmessenger.data.model.UsersResponse

interface ZulipService {

    @GET("users")
    fun getUsers(): Single<UsersResponse>

    @GET("users/{id}")
    fun getUser(@Path("id") id: String): Single<UserResponse>

    @GET("users/{id}/presence")
    fun getUserPresence(@Path("id") id: String): Single<PresenceResponse>
}