package ru.nordbird.tfsmessenger.data.repository.base

import io.reactivex.Single

interface EventRepository {

    fun registerTopicEventQueue(streamName: String, topicName: String): Single<String>

    fun deleteEventQueue(queueId: String): Single<Boolean>

}