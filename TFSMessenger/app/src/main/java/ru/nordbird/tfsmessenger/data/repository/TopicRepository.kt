package ru.nordbird.tfsmessenger.data.repository

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import ru.nordbird.tfsmessenger.data.api.ZulipService
import ru.nordbird.tfsmessenger.data.dao.TopicDao
import ru.nordbird.tfsmessenger.data.mapper.TopicDbToTopicMapper
import ru.nordbird.tfsmessenger.data.mapper.TopicNwToTopicDbMapper
import ru.nordbird.tfsmessenger.data.model.*

class TopicRepository(
    private val apiService: ZulipService,
    private val topicDao: TopicDao
) {

    private val nwTopicMapper = TopicNwToTopicDbMapper()
    private val dbTopicMapper = TopicDbToTopicMapper()

    fun getStreamTopics(streamId: Int, streamName: String): Flowable<List<Topic>> {
        return Single.concat(
            getDatabaseStreamTopics(streamName),
            getNetworkStreamTopics(streamId, streamName)
        )
            .map { dbTopicMapper.transform(it) }
    }

    private fun getNetworkStreamTopics(streamId: Int, streamName: String): Single<List<TopicDb>> {
        return apiService.getStreamTopics(streamId)
            .flatMapObservable { response ->
                Observable.fromIterable(response.topics
                    .map { nwTopicMapper.transform(it) })
            }
            .map { topic ->
                topic.copy(streamName = streamName)
            }
            .toList()
            .doOnSuccess { saveTopicsToDatabase(it) }
    }

    private fun getDatabaseStreamTopics(streamName: String): Single<List<TopicDb>> {
        return topicDao.getByStreamName(streamName)
    }

    private fun saveTopicsToDatabase(topics: List<TopicDb>) {
        topicDao.insertAll(topics)
    }
}