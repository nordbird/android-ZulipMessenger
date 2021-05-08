package ru.nordbird.tfsmessenger.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateStreamRequest(

    @SerialName("name")
    val name: String,

    @SerialName("description")
    val description: String

)
