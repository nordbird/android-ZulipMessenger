package ru.nordbird.tfsmessenger.data.repository.base

import io.reactivex.Flowable
import io.reactivex.Single
import ru.nordbird.tfsmessenger.data.model.Topic

interface TopicRepository {

    fun getStreamTopics(streamId: Int, streamName: String): Flowable<List<Topic>>

    fun getUnreadMessageCount(streamName: String, topicName: String): Single<Int>

}