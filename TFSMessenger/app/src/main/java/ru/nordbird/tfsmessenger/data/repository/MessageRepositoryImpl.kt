package ru.nordbird.tfsmessenger.data.repository

import io.reactivex.Flowable
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import ru.nordbird.tfsmessenger.data.api.MessageQuery
import ru.nordbird.tfsmessenger.data.api.ZulipConst
import ru.nordbird.tfsmessenger.data.api.ZulipService
import ru.nordbird.tfsmessenger.data.dao.MessageDao
import ru.nordbird.tfsmessenger.data.mapper.MessageDbToMessageMapper
import ru.nordbird.tfsmessenger.data.mapper.MessageNwToMessageDbMapper
import ru.nordbird.tfsmessenger.data.model.*
import ru.nordbird.tfsmessenger.data.repository.base.MessageRepository
import java.io.InputStream
import java.util.*

class MessageRepositoryImpl(
    private val apiService: ZulipService,
    private val messageDao: MessageDao
) : MessageRepository {
    companion object {
        private const val MESSAGES_MAX_COUNT = 50
    }

    private val nwMessageMapper = MessageNwToMessageDbMapper()
    private val dbMessageMapper = MessageDbToMessageMapper(ZulipConst.BASE_URL, ZulipConst.UPLOAD_PATH)
    private var maxId = 0

    override fun getMessages(streamName: String, topicName: String, lastMessageId: Int, count: Int): Flowable<List<Message>> {
        return Single.concat(
            getDatabaseMessages(streamName, topicName, lastMessageId, count),
            getNetworkMessages(streamName, topicName, lastMessageId, count)
        )
            .map { dbMessageMapper.transform(it) }
    }

    override fun getMessagesByEvent(streamName: String, topicName: String, lastMessageId: Int, queueId: String): Single<List<Message>> {
        val query = MessageQuery.getNewMessages(streamName, topicName, lastMessageId)

        return apiService.getEvents(queueId).flatMap { apiService.getMessages(query) }.map { response ->
            nwMessageMapper.transform(response.messages)
                .map { message -> message.copy(streamName = streamName) }
        }
            .doOnSuccess { messages -> maxId = maxOf(maxId, messages.maxOfOrNull { it.id } ?: 0) }
            .doOnSuccess { saveToDatabase(streamName, topicName, it) }
            .map { dbMessageMapper.transform(it) }
    }

    override fun getMessageContent(messageId: Int): Single<String> {
        return apiService.getMessageContent(messageId).map { it.content }
    }

    override fun addMessage(streamName: String, topicName: String, senderId: Int, content: String): Flowable<List<Message>> {
        val messageId = ++maxId
        val message = MessageDb(messageId, streamName, topicName, senderId, "", "", content, Date().time, localId = messageId)

        return Flowable.concat(
            Flowable.fromCallable { listOf(saveToDatabase(message)) },
            addNetworkMessage(streamName, topicName, content).toFlowable().flatMap { response ->
                if (response.result == ZulipConst.RESPONSE_RESULT_SUCCESS) {
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

    override fun updateMessage(messageId: Int, topicName: String, content: String): Single<Boolean> {
        val query = MessageQuery.updateMessage(topicName, content)

        return apiService.updateMessage(messageId, query).map { response ->
            val result = response.result == ZulipConst.RESPONSE_RESULT_SUCCESS
            if (!result) throw Exception(response.msg)
            result
        }
            .flatMap { messageDao.getById(messageId) }
            .map { message ->
                updateMessage(message, topicName, content)
                true
            }
    }

    override fun deleteMessage(messageId: Int): Single<Boolean> {
        return apiService.deleteMessage(messageId).map { response ->
            val result = response.result == ZulipConst.RESPONSE_RESULT_SUCCESS
            if (!result) throw Exception(response.msg)
            result
        }
            .doOnSuccess { messageDao.deleteById(messageId) }
    }

    override fun sendFile(streamName: String, topicName: String, senderId: Int, name: String, stream: InputStream?): Flowable<List<Message>> {
        val bytes = stream?.use { it.readBytes() } ?: return Flowable.fromArray(emptyList())

        val requestBody: RequestBody = RequestBody.create(MediaType.parse("*/*"), bytes)
        val fileToUpload = MultipartBody.Part.createFormData("file", name, requestBody)
        return apiService.uploadFile(fileToUpload).toFlowable()
            .flatMap {
                val content = "[$name](${ZulipConst.BASE_URL}${it.uri})"
                addMessage(streamName, topicName, senderId, content)
            }
    }

    override fun downloadFile(url: String): Single<InputStream> {
        return apiService.downloadFile(url).map { it.byteStream() }
    }

    private fun getNetworkMessages(streamName: String, topicName: String, lastMessageId: Int, count: Int): Single<List<MessageDb>> {
        val query = MessageQuery.getMessages(streamName, topicName, lastMessageId, count)

        return apiService.getMessages(query).map { response ->
            nwMessageMapper.transform(response.messages)
                .map { message -> message.copy(streamName = streamName) }
        }
            .doOnSuccess { messages -> maxId = maxOf(maxId, messages.maxOfOrNull { it.id } ?: 0) }
            .doOnSuccess { saveToDatabase(streamName, topicName, it) }
    }

    private fun getDatabaseMessages(streamName: String, topicName: String, lastMessageId: Int, count: Int): Single<List<MessageDb>> {
        return if (topicName.isNotEmpty()) {
            messageDao.getTopicMessages(streamName, topicName, lastMessageId, count)
        } else {
            messageDao.getStreamMessages(streamName, lastMessageId, count)
        }
    }

    private fun addNetworkMessage(streamName: String, topicName: String, text: String): Single<MessageResponse> {
        val query = MessageQuery.addMessage(streamName, topicName, text)
        return apiService.sendMessage(query)
    }

    private fun replaceMessage(message: MessageDb, newMessageId: Int): MessageDb {
        messageDao.deleteById(message.id)
        val newMessage = message.copy(id = newMessageId, localId = message.localId)
        messageDao.insert(newMessage)
        return newMessage
    }

    private fun updateMessage(message: MessageDb, topicName: String, content: String) {
        messageDao.deleteById(message.id)
        val newMessage = message.copy(topicName = topicName, content = content)
        messageDao.insert(newMessage)
    }

    private fun saveToDatabase(streamName: String, topicName: String, messages: List<MessageDb>) {
        messageDao.insertAll(messages)
        if (topicName.isNotEmpty()) {
            messageDao.deleteOverLimit(streamName, topicName, MESSAGES_MAX_COUNT)
        }
    }

    private fun saveToDatabase(message: MessageDb): MessageDb {
        messageDao.insert(message)
        return message
    }
}