package ru.nordbird.tfsmessenger.data.api

import io.reactivex.Single
import retrofit2.http.*
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

    @GET("messages")
    fun getMessages(@QueryMap queryMap: Map<String, String>): Single<MessagesResponse>

    @POST("messages")
    fun sendMessage(@QueryMap queryMap: Map<String, String>): Single<MessageResponse>

    @POST("messages/{id}/reactions")
    fun addMessageReaction(@Path("id") id: Int, @Query("emoji_name") reactionName: String): Single<BaseResponse>

    @DELETE("messages/{id}/reactions")
    fun removeMessageReaction(@Path("id") id: Int, @Query("emoji_name") reactionName: String): Single<BaseResponse>
}