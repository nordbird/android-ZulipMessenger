package ru.nordbird.tfsmessenger.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PresenceNw(

    @SerialName("timestamp")
    val timestamp_sec: Long,

    @SerialName("status")
    val status: String
)

