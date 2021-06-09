package ru.nordbird.tfsmessenger.data.api

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.nordbird.tfsmessenger.data.model.MessagesNarrowRequest

object MessageQuery {
    private const val MESSAGE_ANCHOR_NEWEST = "newest"
    private const val MESSAGE_TYPE_STREAM = "stream"
    private const val MESSAGE_COUNT_ZERO = "0"
    private const val MESSAGE_COUNT_UNREAD = "100"
    private const val MESSAGE_COUNT_MAX = "5000"

    fun getMessages(streamName: String, topicName: String, lastMessageId: Int, count: Int): Map<String, String> {
        val narrow = mutableListOf(
            MessagesNarrowRequest("stream", streamName)
        )
        if (topicName.isNotEmpty()) narrow.add(MessagesNarrowRequest("topic", topicName))

        val anchor = if (lastMessageId > 0) lastMessageId.toString() else MESSAGE_ANCHOR_NEWEST

        return mapOf(
            "anchor" to anchor,
            "num_before" to count.toString(),
            "num_after" to MESSAGE_COUNT_ZERO,
            "narrow" to Json.encodeToString(narrow),
            "apply_markdown" to "false"
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
            "num_before" to MESSAGE_COUNT_UNREAD,
            "num_after" to MESSAGE_COUNT_ZERO,
            "narrow" to Json.encodeToString(narrow)
        )
    }

    fun getNewMessages(streamName: String, topicName: String, lastMessageId: Int): Map<String, String> {
        val narrow = mutableListOf(
            MessagesNarrowRequest("stream", streamName)
        )
        if (topicName.isNotEmpty()) narrow.add(MessagesNarrowRequest("topic", topicName))

        val anchor = if (lastMessageId > 0) lastMessageId.toString() else MESSAGE_ANCHOR_NEWEST

        return mapOf(
            "anchor" to anchor,
            "num_before" to MESSAGE_COUNT_ZERO,
            "num_after" to MESSAGE_COUNT_MAX,
            "narrow" to Json.encodeToString(narrow),
            "apply_markdown" to "false"
        )
    }

    fun addMessage(streamName: String, topicName: String, content: String): Map<String, String> {
        return mapOf(
            "type" to MESSAGE_TYPE_STREAM,
            "to" to streamName,
            "content" to content,
            "topic" to topicName
        )
    }

    fun updateMessage(topicName: String, content: String): Map<String, String> {
        return mapOf(
            "topic" to topicName,
            "content" to content,
        )
    }
}