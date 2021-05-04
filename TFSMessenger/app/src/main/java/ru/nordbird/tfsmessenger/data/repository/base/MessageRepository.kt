package ru.nordbird.tfsmessenger.data.repository.base

import io.reactivex.Flowable
import io.reactivex.Single
import ru.nordbird.tfsmessenger.data.model.*
import java.io.InputStream

interface MessageRepository {

    fun getMessages(streamName: String, topicName: String, lastMessageId: Int, count: Int): Flowable<List<Message>>

    fun getTopicMessagesByEvent(streamName: String, topicName: String, lastMessageId: Int, queueId: String): Single<List<Message>>

    fun getUnreadMessageCount(streamName: String, topicName: String): Single<Int>

    fun addMessage(streamName: String, topicName: String, senderId: Int, text: String): Flowable<List<Message>>

    fun sendFile(streamName: String, topicName: String, senderId: Int, name: String, stream: InputStream?): Flowable<List<Message>>

    fun downloadFile(url: String): Single<InputStream>

}