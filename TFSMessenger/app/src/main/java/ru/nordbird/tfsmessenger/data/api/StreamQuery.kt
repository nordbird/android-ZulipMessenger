package ru.nordbird.tfsmessenger.data.api

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.nordbird.tfsmessenger.data.model.CreateStreamRequest

object StreamQuery {

    fun createStream(streamName: String): Map<String, String> {
        val narrow = listOf(
            CreateStreamRequest(streamName, "")
        )
        return mapOf(
            "subscriptions" to Json.encodeToString(narrow)
        )
    }

}