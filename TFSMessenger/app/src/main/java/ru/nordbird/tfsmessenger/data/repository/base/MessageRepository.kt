package ru.nordbird.tfsmessenger.data.repository.base

import io.reactivex.Flowable
import io.reactivex.Single
import ru.nordbird.tfsmessenger.data.model.*
import java.io.InputStream

interface MessageRepository {

    fun getMessages(streamName: String, topicName: String, lastMessageId: Int, count: Int): Flowable<List<Message>>

    fun getMessagesByEvent(streamName: String, topicName: String, lastMessageId: Int, queueId: String): Single<List<Message>>

    fun getMessageContent(messageId: Int): Single<String>

    fun addMessage(streamName: String, topicName: String, senderId: Int, content: String): Flowable<List<Message>>

    fun updateMessage(messageId: Int, topicName: String, content: String): Single<Boolean>

    fun deleteMessage(messageId: Int): Single<Boolean>

    fun sendFile(streamName: String, topicName: String, senderId: Int, name: String, stream: InputStream?): Flowable<List<Message>>

    fun downloadFile(url: String): Single<InputStream>

}