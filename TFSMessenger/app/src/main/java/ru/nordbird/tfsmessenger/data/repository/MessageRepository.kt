package ru.nordbird.tfsmessenger.data.repository

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import ru.nordbird.tfsmessenger.data.api.ZulipAuth
import ru.nordbird.tfsmessenger.data.api.ZulipService
import ru.nordbird.tfsmessenger.data.dao.AppDatabase
import ru.nordbird.tfsmessenger.data.mapper.MessageDbToMessageMapper
import ru.nordbird.tfsmessenger.data.mapper.MessageNwToMessageDbMapper
import ru.nordbird.tfsmessenger.data.model.*
import java.io.InputStream
import java.util.*

class MessageRepository(
    private val apiService: ZulipService,
    private val dbService: AppDatabase
) {
    companion object {
        private const val MESSAGE_ANCHOR = "newest"
        private const val MESSAGE_TYPE_STREAM = "stream"
        private const val MESSAGE_AFTER_ID = "0"
        private const val MESSAGES_MAX_COUNT = 50
    }

    private val nwMessageMapper = MessageNwToMessageDbMapper()
    private val dbMessageMapper = MessageDbToMessageMapper(ZulipAuth.BASE_URL)

    private var maxId = 0

    fun getMessages(streamName: String, topicName: String, lastMessageId: Int, count: Int): Flowable<List<Message>> {
        return Single.concat(
            getDatabaseMessages(streamName, topicName, lastMessageId, count),
            getNetworkMessages(streamName, topicName, lastMessageId, count)
        )
            .map { dbMessageMapper.transform(it) }
    }

    fun addMessage(streamName: String, topicName: String, senderId: Int, text: String): Flowable<List<Message>> {
        val messageId = ++maxId
        val message = MessageDb(messageId, streamName, topicName, senderId, "", "", text, Date().time, localId = messageId)

        return Flowable.concat(
            Flowable.fromCallable { listOf(saveToDatabase(message)) },
            addNetworkMessage(streamName, topicName, text).toFlowable().flatMap { response ->
                if (response.result == RESPONSE_RESULT_SUCCESS) {
                    Single.concat(
                        Single.fromCallable { listOf(replaceMessage(message, response.id)) },
                        getNetworkMessages(streamName, topicName, response.id, 1)
                    )
                } else {
                    Flowable.fromArray(listOf(message))
                }
            }
        )
            .map { dbMessageMapper.transform(it) }
            .onErrorReturnItem(emptyList())
    }

    fun addReaction(messageId: Int, currentUserId: Int, reactionCode: Int, reactionName: String): Flowable<List<Message>> {
        return Single.concat(
            dbService.messageDao().getById(messageId).map { message ->
                saveToDatabase(addReaction(message, currentUserId, reactionCode, reactionName))
            },
            apiService.addMessageReaction(messageId, reactionName).flatMap { response ->
                if (response.result == RESPONSE_RESULT_SUCCESS) {
                    dbService.messageDao().getById(messageId)
                } else {
                    dbService.messageDao().getById(messageId).map { message ->
                        saveToDatabase(removeReaction(message, currentUserId, reactionName))
                    }
                }
            }
        )
            .map { dbMessageMapper.transform(listOf(it)) }
            .onErrorReturnItem(emptyList())
    }

    fun removeReaction(messageId: Int, currentUserId: Int, reactionCode: Int, reactionName: String): Flowable<List<Message>> {
        return Single.concat(
            dbService.messageDao().getById(messageId).map { message ->
                saveToDatabase(removeReaction(message, currentUserId, reactionName))
            },
            apiService.removeMessageReaction(messageId, reactionName).flatMap { response ->
                if (response.result == RESPONSE_RESULT_SUCCESS) {
                    dbService.messageDao().getById(messageId)
                } else {
                    dbService.messageDao().getById(messageId).map { message ->
                        saveToDatabase(addReaction(message, currentUserId, reactionCode, reactionName))
                    }
                }
            }
        )
            .map { dbMessageMapper.transform(listOf(it)) }
            .onErrorReturnItem(emptyList())
    }

    fun sendFile(streamName: String, topicName: String, senderId: Int, name: String, stream: InputStream?): Flowable<List<Message>> {
        val bytes = stream?.use {
            it.readBytes()
        } ?: return Flowable.fromArray(emptyList())

        val requestBody: RequestBody = RequestBody.create(MediaType.parse("*/*"), bytes)
        val fileToUpload = MultipartBody.Part.createFormData("file", name, requestBody)
        return apiService.uploadFile(fileToUpload).toFlowable()
            .flatMap {
                val content = "[$name](${ZulipAuth.BASE_URL}${it.uri})"
                addMessage(streamName, topicName, senderId, content)
            }
    }

    fun downloadFile(url: String): Single<InputStream> {
        return apiService.downloadFile(url).map {
            it.byteStream()
        }
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

        return apiService.getMessages(query)
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
        return dbService.messageDao().getAll(streamName, topicName, lastMessageId, count)
    }

    private fun addNetworkMessage(streamName: String, topicName: String, text: String): Single<MessageResponse> {
        val query = mapOf(
            "type" to MESSAGE_TYPE_STREAM,
            "to" to streamName,
            "content" to text,
            "topic" to topicName
        )
        return apiService.sendMessage(query)
    }

    private fun replaceMessage(message: MessageDb, newMessageId: Int): MessageDb {
        dbService.messageDao().deleteById(message.id)
        val newMessage = message.copy(id = newMessageId, localId = message.localId)
        dbService.messageDao().insert(newMessage)
        return newMessage
    }

    private fun addReaction(message: MessageDb, senderId: Int, reactionCode: Int, reactionName: String): MessageDb {
        val list = message.reactions.toMutableList()
        list.add(Reaction(reactionCode.toString(16), reactionName, senderId))
        return message.copy(reactions = list)
    }

    private fun removeReaction(message: MessageDb, senderId: Int, reactionName: String): MessageDb {
        val list = message.reactions.filterNot { it.userId == senderId && it.name == reactionName }
        return message.copy(reactions = list)
    }

    private fun saveToDatabase(streamName: String, topicName: String, messages: List<MessageDb>) {
        dbService.messageDao().insertAll(messages)
        dbService.messageDao().deleteOverLimit(streamName, topicName, MESSAGES_MAX_COUNT)
    }

    private fun saveToDatabase(message: MessageDb): MessageDb {
        dbService.messageDao().insert(message)
        return message
    }
}