package ru.nordbird.tfsmessenger.data.api

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.nordbird.tfsmessenger.data.model.MessagesNarrowRequest

object MessageQuery {
    private const val MESSAGE_ANCHOR_NEWEST = "newest"
    private const val MESSAGE_TYPE_STREAM = "stream"
    private const val MESSAGE_AFTER_ID = "0"
    private const val MESSAGE_BEFORE_ID = "100"

    fun getMessages(streamName: String, topicName: String, lastMessageId: Int, count: Int): Map<String, String> {
        val narrow = listOf(
            MessagesNarrowRequest("stream", streamName),
            MessagesNarrowRequest("topic", topicName)
        )
        val anchor = if (lastMessageId > 0) lastMessageId.toString() else MESSAGE_ANCHOR_NEWEST

        return mapOf(
            "anchor" to anchor,
            "num_before" to count.toString(),
            "num_after" to MESSAGE_AFTER_ID,
            "narrow" to Json.encodeToString(narrow)
        )
    }

    fun getUnreadMessages(streamName: String, topicName: String): Map<String, String> {
        val narrow = listOf(
            MessagesNarrowRequest("stream", streamName),
            MessagesNarrowRequest("topic", topicName),
            MessagesNarrowRequest("is", "unread")
        )

        return mapOf(
            "anchor" to MESSAGE_ANCHOR_NEWEST,
            "num_before" to MESSAGE_BEFORE_ID,
            "num_after" to MESSAGE_AFTER_ID,
            "narrow" to Json.encodeToString(narrow)
        )
    }

    fun addMessage(streamName: String, topicName: String, text: String): Map<String, String> {
        return mapOf(
            "type" to MESSAGE_TYPE_STREAM,
            "to" to streamName,
            "content" to text,
            "topic" to topicName
        )
    }
}