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
import ru.nordbird.tfsmessenger.data.api.MessageQuery
import ru.nordbird.tfsmessenger.data.api.ZulipAuth
import ru.nordbird.tfsmessenger.data.api.ZulipService
import ru.nordbird.tfsmessenger.data.dao.MessageDao
import ru.nordbird.tfsmessenger.data.mapper.MessageDbToMessageMapper
import ru.nordbird.tfsmessenger.data.mapper.MessageNwToMessageDbMapper
import ru.nordbird.tfsmessenger.data.model.*
import java.io.InputStream
import java.util.*

class MessageRepository(
    private val apiService: ZulipService,
    private val messageDao: MessageDao
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
        val query = MessageQuery.getMessages(streamName, topicName, lastMessageId, count)

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
        return messageDao.getTopicMessages(streamName, topicName, lastMessageId, count)
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
        messageDao.deleteById(message.id)
        val newMessage = message.copy(id = newMessageId, localId = message.localId)
        messageDao.insert(newMessage)
        return newMessage
    }

    private fun saveToDatabase(streamName: String, topicName: String, messages: List<MessageDb>) {
        messageDao.insertAll(messages)
        messageDao.deleteOverLimit(streamName, topicName, MESSAGES_MAX_COUNT)
    }

    private fun saveToDatabase(message: MessageDb): MessageDb {
        messageDao.insert(message)
        return message
    }
}