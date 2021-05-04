package ru.nordbird.tfsmessenger.data.repository.base

import io.reactivex.Flowable
import ru.nordbird.tfsmessenger.data.model.Topic

interface TopicRepository {

    fun getStreamTopics(streamId: Int, streamName: String): Flowable<List<Topic>>

}