package ru.nordbird.tfsmessenger.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class User(

    @SerialName("user_id")
    val id: Int,

    @SerialName("full_name")
    val full_name: String,

    @SerialName("email")
    val email: String,

    @SerialName("avatar_url")
    val avatar_url: String,

    @Transient
    var presence: Presence = Presence(0, "idle")
)
