package ru.nordbird.tfsmessenger.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessagesNarrowRequest(

    @SerialName("operator")
    val operator: String,

    @SerialName("operand")
    val operand: String

)
