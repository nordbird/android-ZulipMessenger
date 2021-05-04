package ru.nordbird.tfsmessenger.data.api

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object EventQuery {

    fun registerTopicEvent(streamName: String, topicName: String): Map<String, String> {
        val narrow = listOf(
            listOf("stream", streamName),
            listOf("topic", topicName)
        )
        return mapOf(
            "narrow" to Json.encodeToString(narrow)
        )
    }

}