package ru.nordbird.tfsmessenger.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PresenceResponse(
    @SerialName("presence")
    val presence: Map<String, Presence> = emptyMap()
)