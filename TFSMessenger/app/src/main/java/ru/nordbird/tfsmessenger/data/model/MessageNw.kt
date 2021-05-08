package ru.nordbird.tfsmessenger.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageNw(

    @SerialName("id")
    val id: Int,

    @SerialName("sender_id")
    val authorId: Int,

    @SerialName("sender_full_name")
    val authorName: String,

    @SerialName("avatar_url")
    val avatar_url: String,

    @SerialName("content")
    val content: String,

    @SerialName("timestamp")
    val timestamp_sec: Long,

    @SerialName("reactions")
    val reactions: List<Reaction>,

    @SerialName("subject")
    val topicName: String

)