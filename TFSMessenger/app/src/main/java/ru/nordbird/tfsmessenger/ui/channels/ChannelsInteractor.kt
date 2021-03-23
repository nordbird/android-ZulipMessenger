package ru.nordbird.tfsmessenger.ui.channels

import ru.nordbird.tfsmessenger.data.mapper.StreamToStreamUiMapper
import ru.nordbird.tfsmessenger.data.mapper.TopicToTopicUiMapper
import ru.nordbird.tfsmessenger.data.model.Topic
import ru.nordbird.tfsmessenger.data.repository.StreamRepository
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped
import ru.nordbird.tfsmessenger.ui.recycler.holder.StreamUi
import ru.nordbird.tfsmessenger.ui.recycler.holder.TopicUi

object ChannelsInteractor {

    private val streamRepository = StreamRepository
    private val streamMapper = StreamToStreamUiMapper()
    private val topicMapper = TopicToTopicUiMapper()

    private val allStreams = mutableListOf<ViewTyped>()
    private val subscribedStreams = mutableListOf<ViewTyped>()

    private val allTopics = mutableMapOf<String, List<Topic>>()
    private val subscribedTopics = mutableMapOf<String, List<Topic>>()

    private var filterQueryAllStreams = ""
    private var filterQuerySubscribedStreams = ""

    fun getAllStreams(): List<ViewTyped> {
        val streamList = streamRepository.getAllStreams().filter {
            it.name.contains(filterQueryAllStreams, true)
        }
        val streamAndTopicList = streamMapper.transform(streamList).flatMap { makeStreamWithTopics(it, allTopics) }
        allStreams.clear()
        allStreams.addAll(streamAndTopicList)
        return allStreams
    }

    fun getSubscribedStreams(): List<ViewTyped> {
        val streamList = streamRepository.getSubscribedStreams().filter {
            it.name.contains(filterQuerySubscribedStreams, true)
        }
        val streamAndTopicList = streamMapper.transform(streamList).flatMap { makeStreamWithTopics(it, subscribedTopics) }
        subscribedStreams.clear()
        subscribedStreams.addAll(streamAndTopicList)
        return subscribedStreams
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

    fun getAllStreamsTopic(itemId: String): TopicUi? {
        return getTopic(allStreams, itemId)
    }

    fun getSubscribedStreamsTopic(itemId: String): TopicUi? {
        return getTopic(subscribedStreams, itemId)
    }

    fun getAllStream(streamId: String): StreamUi? {
        return getStream(allStreams, streamId)
    }

    fun getSubscribedStream(streamId: String): StreamUi? {
        return getStream(subscribedStreams, streamId)
    }

    private fun getStream(streamList: List<ViewTyped>, streamId: String): StreamUi? {
        return streamList.filterIsInstance<StreamUi>().firstOrNull { it.id == streamId }
    }

    private fun getTopic(streamList: List<ViewTyped>, itemId: String): TopicUi? {
        return streamList.filterIsInstance<TopicUi>().firstOrNull { it.uid == itemId }
    }

    private fun updateStreamTopics(
        streamList: List<ViewTyped>,
        topicMap: MutableMap<String, List<Topic>>,
        streamId: String
    ) {
        val stream = streamList.filterIsInstance<StreamUi>().firstOrNull { it.id == streamId } ?: return

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