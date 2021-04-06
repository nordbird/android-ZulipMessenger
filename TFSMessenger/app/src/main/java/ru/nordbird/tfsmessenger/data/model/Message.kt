package ru.nordbird.tfsmessenger.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Message(

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
    val date: Long,

    @SerialName("reactions")
    var reactions: List<Reaction> = listOf(),

    @Transient
    var isIncoming: Boolean = false
)