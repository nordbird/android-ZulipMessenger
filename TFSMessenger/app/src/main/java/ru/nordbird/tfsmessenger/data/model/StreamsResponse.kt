package ru.nordbird.tfsmessenger.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StreamsResponse(

    @SerialName("streams")
    val streams: List<Stream>
)
