package ru.nordbird.tfsmessenger.domain

import io.reactivex.Flowable
import io.reactivex.Single
import ru.nordbird.tfsmessenger.data.mapper.StreamToStreamUiMapper
import ru.nordbird.tfsmessenger.data.mapper.TopicToTopicUiMapper
import ru.nordbird.tfsmessenger.data.model.UnreadCounter
import ru.nordbird.tfsmessenger.data.repository.base.StreamRepository
import ru.nordbird.tfsmessenger.data.repository.base.TopicRepository
import ru.nordbird.tfsmessenger.domain.base.ChannelsInteractor
import ru.nordbird.tfsmessenger.ui.recycler.holder.StreamUi
import ru.nordbird.tfsmessenger.ui.recycler.holder.TopicUi

class ChannelsInteractorImpl(
    private val streamRepository: StreamRepository,
    private val topicRepository: TopicRepository
) : ChannelsInteractor {

    private val streamMapper = StreamToStreamUiMapper()
    private val topicMapper = TopicToTopicUiMapper()

    override fun loadStreams(): Flowable<List<StreamUi>> {
        return streamRepository.getStreams()
            .map { streams ->
                streamMapper.transform(streams)
            }
    }

    override fun loadSubscriptions(): Flowable<List<StreamUi>> {
        return streamRepository.getSubscriptions()
            .map { streams ->
                streamMapper.transform(streams)
            }
    }

    override fun loadTopics(streamId: Int, streamName: String): Flowable<List<TopicUi>> {
        return topicRepository.getStreamTopics(streamId, streamName)
            .map { topics ->
                topicMapper.transform(topics)
            }
    }

    override fun getTopicUnreadMessageCount(streamName: String, topicName: String): Single<UnreadCounter> {
        return topicRepository.getUnreadMessageCount(streamName, topicName)
            .map { count ->
                UnreadCounter(streamName, topicName, count)
            }
    }

    override fun createStream(streamName: String): Single<Boolean> {
        return streamRepository.createStream(streamName)
    }

}