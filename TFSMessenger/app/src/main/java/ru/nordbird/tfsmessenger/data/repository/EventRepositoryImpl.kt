package ru.nordbird.tfsmessenger.data.repository

import io.reactivex.Single
import ru.nordbird.tfsmessenger.data.api.EventQuery
import ru.nordbird.tfsmessenger.data.api.ZulipConst.RESPONSE_RESULT_SUCCESS
import ru.nordbird.tfsmessenger.data.api.ZulipService
import ru.nordbird.tfsmessenger.data.repository.base.EventRepository

class EventRepositoryImpl(
    private val apiService: ZulipService
): EventRepository {

    override fun registerTopicEventQueue(streamName: String, topicName: String): Single<String> {
        val query = EventQuery.registerTopicEvent(streamName, topicName)
        return apiService.registerEventQueue(query).map { it.queue_id }
    }

    override fun deleteEventQueue(queueId: String): Single<Boolean> {
        return apiService.deleteEventQueue(queueId).map { it.result == RESPONSE_RESULT_SUCCESS }
    }

}