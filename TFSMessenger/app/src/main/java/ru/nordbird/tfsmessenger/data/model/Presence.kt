package ru.nordbird.tfsmessenger.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Presence(

    @SerialName("timestamp")
    val timestamp: Int,

    @SerialName("status")
    val status: String
)

