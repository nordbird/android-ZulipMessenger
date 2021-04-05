package ru.nordbird.tfsmessenger.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TopicsResponse(

    @SerialName("topics")
    val topics: List<Topic>
)
