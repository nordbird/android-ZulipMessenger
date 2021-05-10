package ru.nordbird.tfsmessenger.domain.base

import io.reactivex.Flowable
import io.reactivex.Single
import ru.nordbird.tfsmessenger.ui.recycler.holder.MessageUi
import java.io.InputStream

interface TopicInteractor {

    fun loadMessages(streamName: String, topicName: String, messageId: Int): Flowable<List<MessageUi>>

    fun loadMessagesByEvent(streamName: String, topicName: String, messageId: Int, queueId: String): Single<List<MessageUi>>

    fun loadMessageContent(messageId: Int): Single<String>

    fun addMessage(streamName: String, topicName: String, content: String): Flowable<List<MessageUi>>

    fun updateMessage(messageId: Int, topicName: String, content: String): Single<Boolean>

    fun deleteMessage(messageId: Int): Single<Boolean>

    fun updateReaction(message: MessageUi, currentUserId: Int, reactionCode: String): Flowable<List<MessageUi>>

    fun sendFile(streamName: String, topicName: String, name: String, stream: InputStream?): Flowable<List<MessageUi>>

    fun downloadFile(url: String): Single<InputStream>

}