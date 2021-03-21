package ru.nordbird.tfsmessenger.ui.channels

import ru.nordbird.tfsmessenger.data.mapper.StreamToStreamUiMapper
import ru.nordbird.tfsmessenger.data.mapper.TopicToTopicUiMapper
import ru.nordbird.tfsmessenger.data.model.Topic
import ru.nordbird.tfsmessenger.data.repository.StreamRepository
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped
import ru.nordbird.tfsmessenger.ui.recycler.holder.StreamUi

object ChannelsInteractor {

    private val streamRepository = StreamRepository
    private val streamMapper = StreamToStreamUiMapper()
    private val topicMapper = TopicToTopicUiMapper()

    private val allStreams = mutableListOf<StreamUi>()
    private val subscribedStreams = mutableListOf<StreamUi>()

    private val allTopics = mutableMapOf<String, List<Topic>>()
    private val subscribedTopics = mutableMapOf<String, List<Topic>>()

    private var filterQueryAllStreams = ""
    private var filterQuerySubscribedStreams = ""

    fun getAllStreams(): List<ViewTyped> {
        val newList = streamRepository.getAllStreams().filter {
            it.name.contains(filterQueryAllStreams, true)
        }
        allStreams.clear()
        allStreams.addAll(streamMapper.transform(newList))
        return allStreams.flatMap { makeStreamWithTopics(it, allTopics) }
    }

    fun getSubscribedStreams(): List<ViewTyped> {
        val newList = streamRepository.getSubscribedStreams().filter {
            it.name.contains(filterQuerySubscribedStreams, true)
        }
        subscribedStreams.clear()
        subscribedStreams.addAll(streamMapper.transform(newList))
        return subscribedStreams.flatMap { makeStreamWithTopics(it, subscribedTopics) }
    }

    fun updateAllStreamTopics(streamId: String) {
        updateStreamTopics(allStreams, allTopics, streamId)
    }

    fun updateSubscribedStreamTopics(streamId: String) {
        updateStreamTopics(subscribedStreams, subscribedTopics, streamId)
    }

    fun filterAllStreams(query: String) {
        filterQueryAllStreams = query
    }

    fun filterSubscribedStreams(query: String) {
        filterQuerySubscribedStreams = query
    }

    private fun updateStreamTopics(
        streamList: List<StreamUi>,
        topicMap: MutableMap<String, List<Topic>>,
        streamId: String
    ) {
        val stream = streamList.firstOrNull { it.id == streamId } ?: return

        if (!stream.topicExpanded) {
            val topicList = streamRepository.getStreamTopics(streamId)
            topicMap[streamId] = topicList
        } else {
            topicMap.remove(streamId)
        }
    }

    private fun makeStreamWithTopics(stream: StreamUi, topicMap: Map<String, List<Topic>>): List<ViewTyped> {
        return if (topicMap.containsKey(stream.id)) {
            stream.topicExpanded = true
            val topicList = topicMap[stream.id]!!
            listOf(stream) + topicMapper.transform(topicList)
        } else listOf(stream)
    }

}