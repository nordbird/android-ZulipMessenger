package ru.nordbird.tfsmessenger.data.api

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import ru.nordbird.tfsmessenger.data.model.*

interface ZulipService {

    @GET("users")
    fun getUsers(): Single<UsersResponse>

    @GET("users/{id}")
    fun getUser(@Path("id") id: String): Single<UserResponse>

    @GET("users/{id}/presence")
    fun getUserPresence(@Path("id") id: String): Single<PresenceResponse>

    @GET("streams")
    fun getStreams(): Single<StreamsResponse>

    @GET("users/me/subscriptions")
    fun getSubscriptions(): Single<SubscriptionsResponse>

    @GET("users/me/{id}/topics")
    fun getStreamTopics(@Path("id") id: String): Single<TopicsResponse>

}