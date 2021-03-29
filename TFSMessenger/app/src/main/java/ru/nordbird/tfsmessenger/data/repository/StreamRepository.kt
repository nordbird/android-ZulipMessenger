package ru.nordbird.tfsmessenger.data.repository

import ru.nordbird.tfsmessenger.data.DataGenerator

object StreamRepository {

    private val allStreams = DataGenerator.getAllStreams()
    private val subscribedStreams = DataGenerator.getSubscribedStreams()

    fun getAllStreams() = allStreams

    fun getSubscribedStreams() = subscribedStreams

    fun getStreamTopics(streamId: String) = DataGenerator.getTopics(streamId)

}