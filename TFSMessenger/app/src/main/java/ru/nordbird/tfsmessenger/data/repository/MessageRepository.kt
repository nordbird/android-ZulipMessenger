package ru.nordbird.tfsmessenger.data.repository

import io.reactivex.Single
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.nordbird.tfsmessenger.data.DataGenerator
import ru.nordbird.tfsmessenger.data.api.ZulipServiceImpl
import ru.nordbird.tfsmessenger.data.model.*

object MessageRepository {
    private const val MESSAGE_ANCHOR = "newest"
    private const val MESSAGE_BEFORE_ID = "100"
    private const val MESSAGE_AFTER_ID = "0"
    private const val MESSAGE_TYPE_STREAM = "stream"

    fun getMessages(streamName: String, topicName: String): Single<List<Message>> {
        val narrow = listOf(
            MessagesNarrowRequest("stream", streamName),
            MessagesNarrowRequest("topic", topicName)
        )

        val query = mapOf(
            "anchor" to MESSAGE_ANCHOR,
            "num_before" to MESSAGE_BEFORE_ID,
            "num_after" to MESSAGE_AFTER_ID,
            "narrow" to Json.encodeToString(narrow)
        )

        return ZulipServiceImpl.getApi().getMessages(query).map { it.messages }
    }

    fun addMessage(streamName: String, topicName: String, text: String): Single<BaseResponse> {
        val query = mapOf(
            "type" to MESSAGE_TYPE_STREAM,
            "to" to streamName,
            "content" to text,
            "topic" to topicName
        )
        return ZulipServiceImpl.getApi().sendMessage(query)
    }

    fun addReaction(messageId: String, reactionName: String): Single<BaseResponse> {
        return ZulipServiceImpl.getApi().addMessageReaction(messageId, reactionName)
    }

    fun removeReaction(messageId: String, reactionName: String): Single<BaseResponse> {
        return ZulipServiceImpl.getApi().removeMessageReaction(messageId, reactionName)
    }


}