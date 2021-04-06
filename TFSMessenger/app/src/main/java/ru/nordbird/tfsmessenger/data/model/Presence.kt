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

const val PRESENCE_STATUS_ACTIVE = "active"
const val PRESENCE_STATUS_IDLE = "idle"
