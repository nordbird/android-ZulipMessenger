package ru.nordbird.tfsmessenger.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse(

    @SerialName("msg")
    val msg: String,

    @SerialName("result")
    val result: String
)