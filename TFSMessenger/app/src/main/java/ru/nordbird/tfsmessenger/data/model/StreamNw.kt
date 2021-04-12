package ru.nordbird.tfsmessenger.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StreamNw(

    @SerialName("stream_id")
    val id: Int,

    @SerialName("name")
    val name: String
)