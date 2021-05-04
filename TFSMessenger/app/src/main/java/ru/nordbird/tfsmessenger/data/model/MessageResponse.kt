package ru.nordbird.tfsmessenger.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageResponse(

    @SerialName("id")
    val id: Int,

    @SerialName("msg")
    val msg: String,

    @SerialName("result")
    val result: String
)