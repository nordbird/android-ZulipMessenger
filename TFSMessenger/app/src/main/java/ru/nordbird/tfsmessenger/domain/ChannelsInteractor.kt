package ru.nordbird.tfsmessenger.domain

import io.reactivex.Flowable
import io.reactivex.Single
import ru.nordbird.tfsmessenger.data.mapper.StreamToStreamUiMapper
import ru.nordbird.tfsmessenger.data.mapper.TopicToTopicUiMapper
import ru.nordbird.tfsmessenger.data.model.Stream
import ru.nordbird.tfsmessenger.data.repository.MessageRepository
import ru.nordbird.tfsmessenger.data.repository.StreamRepository
import ru.nordbird.tfsmessenger.data.repository.TopicRepository
import ru.nordbird.tfsmessenger.ui.channels.ChannelsTabType
import ru.nordbird.tfsmessenger.ui.recycler.holder.StreamUi
import ru.nordbird.tfsmessenger.ui.recycler.holder.TopicUi

class ChannelsInteractor(
    private val tabType: ChannelsTabType,
    private val streamRepository: StreamRepository,
    private val topicRepository: TopicRepository,
    private val messageRepository: MessageRepository
) {

    private val streamMapper = StreamToStreamUiMapper()
    private val topicMapper = TopicToTopicUiMapper()

    fun loadStreams(query: String = ""): Flowable<List<StreamUi>> {
        return getStreamsFun(query)
            .map { streams ->
                streamMapper.transform(streams)
                    .sortedBy { it.name }
            }
    }

    fun loadTopics(streamId: Int, streamName: String): Flowable<List<TopicUi>> {
        return topicRepository.getStreamTopics(streamId, streamName)
            .map { topics ->
                topicMapper.transform(topics)
                    .sortedBy { it.name }
            }
    }

    fun getTopicUnreadMessageCount(streamName: String, topicName: String): Single<Int> {
        return messageRepository.getUnreadMessageCount(streamName, topicName)
    }

    private fun getStreamsFun(query: String = ""): Flowable<List<Stream>> {
        return when (tabType) {
            ChannelsTabType.ALL -> streamRepository.getStreams(query)
            ChannelsTabType.SUBSCRIBED -> streamRepository.getSubscriptions(query)
        }
    }
}