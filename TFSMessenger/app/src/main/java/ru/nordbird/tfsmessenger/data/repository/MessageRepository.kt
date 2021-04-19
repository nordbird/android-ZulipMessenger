package ru.nordbird.tfsmessenger.data.repository

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.nordbird.tfsmessenger.data.api.ZulipServiceImpl
import ru.nordbird.tfsmessenger.data.dao.AppDatabaseImpl
import ru.nordbird.tfsmessenger.data.mapper.MessageDbToMessageMapper
import ru.nordbird.tfsmessenger.data.mapper.MessageNwToMessageDbMapper
import ru.nordbird.tfsmessenger.data.model.*
import java.util.*

object MessageRepository {
    private const val MESSAGE_ANCHOR = "newest"
    private const val MESSAGE_TYPE_STREAM = "stream"
    private const val MESSAGE_AFTER_ID = "0"
    private const val MESSAGES_MAX_COUNT = 50

    private val nwMessageMapper = MessageNwToMessageDbMapper()
    private val dbMessageMapper = MessageDbToMessageMapper()

    private var maxId = 0

    fun getMessages(streamName: String, topicName: String, lastMessageId: Int, count: Int): Single<List<Message>> {
        return Single.concat(
            getDatabaseMessages(streamName, topicName, lastMessageId, count),
            getNetworkMessages(streamName, topicName, lastMessageId, count)
                .onErrorReturnItem(emptyList())
        )
            .filter { it.isNotEmpty() }
            .first(emptyList())
            .map { dbMessageMapper.transform(it) }
    }

    fun addMessage(streamName: String, topicName: String, senderId: String, text: String): Flowable<List<Message>> {
        val messageId = ++maxId
        val message = MessageDb(messageId, streamName, topicName, senderId.toIntOrNull() ?: 0, "", "", text, Date().time)

        return Single.concat(
            Single.fromCallable { saveToDatabase(message) },
            addNetworkMessage(streamName, topicName, text).flatMap {
                if (it.result == RESPONSE_RESULT_SUCCESS) {
                    Single.fromCallable { replaceMessage(message, it.id) }
                } else {
                    Single.just(message)
                }
            }
        )
            .map { dbMessageMapper.transform(listOf(it)) }
            .onErrorReturnItem(emptyList())
    }

    fun addReaction(message: Message, currentUserId: String, reactionCode: Int, reactionName: String): Flowable<List<Message>> {
        val list = message.reactions.toMutableList()
        list.add(Reaction(reactionCode.toString(16), reactionName, currentUserId.toIntOrNull() ?: 0))

        return Single.concat(
            AppDatabaseImpl.messageDao().getById(message.id).map { saveToDatabase(it.copy(reactions = list)) },
            ZulipServiceImpl.getApi().addMessageReaction(message.id, reactionName).flatMap { response ->
                if (response.result == RESPONSE_RESULT_SUCCESS) {
                    AppDatabaseImpl.messageDao().getById(message.id)
                } else {
                    AppDatabaseImpl.messageDao().getById(message.id).map { saveToDatabase(it.copy(reactions = message.reactions)) }
                }
            }
        )
            .map { dbMessageMapper.transform(listOf(it)) }
            .onErrorReturnItem(emptyList())
    }

    fun removeReaction(message: Message, currentUserId: String, reactionName: String): Flowable<List<Message>> {
        val list = message.reactions.filterNot { it.userId.toString() == currentUserId && it.name == reactionName }

        return Single.concat(
            AppDatabaseImpl.messageDao().getById(message.id).map { saveToDatabase(it.copy(reactions = list)) },
            ZulipServiceImpl.getApi().removeMessageReaction(message.id, reactionName).flatMap { response ->
                if (response.result == RESPONSE_RESULT_SUCCESS) {
                    AppDatabaseImpl.messageDao().getById(message.id)
                } else {
                    AppDatabaseImpl.messageDao().getById(message.id).map { saveToDatabase(it.copy(reactions = message.reactions)) }
                }
            }
        )
            .map { dbMessageMapper.transform(listOf(it)) }
            .onErrorReturnItem(emptyList())
    }

    private fun getNetworkMessages(streamName: String, topicName: String, lastMessageId: Int, count: Int): Single<List<MessageDb>> {
        val narrow = listOf(
            MessagesNarrowRequest("stream", streamName),
            MessagesNarrowRequest("topic", topicName)
        )

        val anchor = if (lastMessageId > 0) lastMessageId.toString() else MESSAGE_ANCHOR
        val query = mapOf(
            "anchor" to anchor,
            "num_before" to count.toString(),
            "num_after" to MESSAGE_AFTER_ID,
            "narrow" to Json.encodeToString(narrow)
        )

        return ZulipServiceImpl.getApi().getMessages(query)
            .observeOn(Schedulers.computation())
            .map { nwMessageMapper.transform(it.messages) }
            .flatMapObservable { Observable.fromIterable(it) }
            .map { message ->
                message.streamName = streamName
                message.topicName = topicName
                message
            }
            .doOnNext { maxId = maxOf(maxId, it.id) }
            .toList()
            .doOnSuccess { saveToDatabase(streamName, topicName, it) }
    }

    private fun getDatabaseMessages(streamName: String, topicName: String, lastMessageId: Int, count: Int): Single<List<MessageDb>> {
        return AppDatabaseImpl.messageDao().getAll(streamName, topicName, lastMessageId, count)
    }

    private fun addNetworkMessage(streamName: String, topicName: String, text: String): Single<MessageResponse> {
        val query = mapOf(
            "type" to MESSAGE_TYPE_STREAM,
            "to" to streamName,
            "content" to text,
            "topic" to topicName
        )
        return ZulipServiceImpl.getApi().sendMessage(query)
    }

    private fun replaceMessage(message: MessageDb, newMessageId: Int): MessageDb {
        AppDatabaseImpl.messageDao().deleteById(message.id)
        val newMessage = message.copy(id = newMessageId)
        AppDatabaseImpl.messageDao().insert(newMessage)
        return newMessage
    }

    private fun saveToDatabase(streamName: String, topicName: String, messages: List<MessageDb>) {
        AppDatabaseImpl.messageDao().insertAll(messages)
        AppDatabaseImpl.messageDao().deleteOverLimit(streamName, topicName, MESSAGES_MAX_COUNT)
    }

    private fun saveToDatabase(message: MessageDb): MessageDb {
        AppDatabaseImpl.messageDao().insert(message)
        return message
    }
}