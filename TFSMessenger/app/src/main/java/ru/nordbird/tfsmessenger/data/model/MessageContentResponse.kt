package ru.nordbird.tfsmessenger.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageContentResponse(

    @SerialName("raw_content")
    val content: String
)