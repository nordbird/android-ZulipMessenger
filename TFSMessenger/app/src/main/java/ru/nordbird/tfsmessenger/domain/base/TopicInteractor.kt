package ru.nordbird.tfsmessenger.domain.base

import io.reactivex.Flowable
import io.reactivex.Single
import ru.nordbird.tfsmessenger.ui.recycler.holder.MessageUi
import java.io.InputStream

interface TopicInteractor {

    fun loadMessages(streamName: String, topicName: String, messageId: Int): Flowable<List<MessageUi>>

    fun loadMessagesByEvent(streamName: String, topicName: String, messageId: Int, queueId: String): Single<List<MessageUi>>

    fun addMessage(streamName: String, topicName: String, text: String): Flowable<List<MessageUi>>

    fun updateReaction(message: MessageUi, currentUserId: Int, reactionCode: String): Flowable<List<MessageUi>>

    fun sendFile(streamName: String, topicName: String, name: String, stream: InputStream?): Flowable<List<MessageUi>>

    fun downloadFile(url: String): Single<InputStream>

}