package ru.nordbird.tfsmessenger.data.api

import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import ru.nordbird.tfsmessenger.data.model.*


interface ZulipService {

    @GET("users")
    fun getUsers(): Single<UsersResponse>

    @GET("users/{id}")
    fun getUser(@Path("id") id: Int): Single<UserResponse>

    @GET("users/{id}/presence")
    fun getUserPresence(@Path("id") id: Int): Single<PresenceResponse>

    @GET("streams")
    fun getStreams(): Single<StreamsResponse>

    @GET("users/me/subscriptions")
    fun getSubscriptions(): Single<SubscriptionsResponse>

    @GET("users/me/{id}/topics")
    fun getStreamTopics(@Path("id") id: Int): Single<TopicsResponse>

    @GET("messages")
    fun getMessages(@QueryMap queryMap: Map<String, String>): Single<MessagesResponse>

    @POST("messages")
    fun sendMessage(@QueryMap queryMap: Map<String, String>): Single<MessageResponse>

    @POST("messages/{id}/reactions")
    fun addMessageReaction(@Path("id") id: Int, @Query("emoji_name") reactionName: String): Single<BaseResponse>

    @DELETE("messages/{id}/reactions")
    fun removeMessageReaction(@Path("id") id: Int, @Query("emoji_name") reactionName: String): Single<BaseResponse>

    @Multipart
    @POST("user_uploads")
    fun uploadFile(@Part file: MultipartBody.Part): Single<UploadResponse>

    @Streaming
    @GET
    fun downloadFile(@Url url: String): Single<ResponseBody>

    @POST("register")
    fun registerEventQueue(@QueryMap queryMap: Map<String, String>): Single<EventResponse>

    @DELETE("events")
    fun deleteEventQueue(@Query("queue_id") queueId: String): Single<BaseResponse>

    @GET("events")
    fun getEvents(@Query("queue_id") queueId: String, @Query("last_event_id") lastEventId: String = "-1"): Single<EventResponse>
}