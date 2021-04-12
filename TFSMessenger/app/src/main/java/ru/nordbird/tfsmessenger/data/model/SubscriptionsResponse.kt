package ru.nordbird.tfsmessenger.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionsResponse(

    @SerialName("subscriptions")
    val subscriptions: List<StreamNw>
)
