package ru.nordbird.tfsmessenger.data.repository

import ru.nordbird.tfsmessenger.data.DataGenerator
import ru.nordbird.tfsmessenger.data.model.Stream

object StreamRepository {

    private val allStreams = mutableListOf<Stream>()
    private val subscribedStreams = mutableListOf<Stream>()

    init {
        allStreams.addAll(DataGenerator.getAllStreams())
        subscribedStreams.addAll(DataGenerator.getSubscribedStreams())
    }

    fun getAllStreams() = allStreams.toList()

    fun getSubscribedStreams() = subscribedStreams.toList()

    fun getStreamTopics(streamId: String) = DataGenerator.getTopics(streamId)

}