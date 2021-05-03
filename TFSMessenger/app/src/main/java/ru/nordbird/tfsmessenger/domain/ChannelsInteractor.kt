package ru.nordbird.tfsmessenger.domain

import io.reactivex.Flowable
import io.reactivex.Single
import ru.nordbird.tfsmessenger.data.mapper.StreamToStreamUiMapper
import ru.nordbird.tfsmessenger.data.mapper.TopicToTopicUiMapper
import ru.nordbird.tfsmessenger.data.model.Stream
import ru.nordbird.tfsmessenger.data.model.UnreadCounter
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

    fun loadStreams(): Flowable<List<StreamUi>> {
        return getStreamsFun()
            .map { streams ->
                streamMapper.transform(streams)
            }
    }

    fun loadTopics(streamId: Int, streamName: String): Flowable<List<TopicUi>> {
        return topicRepository.getStreamTopics(streamId, streamName)
            .map { topics ->
                topicMapper.transform(topics)
            }
    }

    fun getTopicUnreadMessageCount(streamName: String, topicName: String): Single<UnreadCounter> {
        return messageRepository.getUnreadMessageCount(streamName, topicName)
            .map { count ->
                UnreadCounter(streamName, topicName, count)
            }
    }

    private fun getStreamsFun(): Flowable<List<Stream>> {
        return when (tabType) {
            ChannelsTabType.ALL -> streamRepository.getStreams()
            ChannelsTabType.SUBSCRIBED -> streamRepository.getSubscriptions()
        }
    }
}