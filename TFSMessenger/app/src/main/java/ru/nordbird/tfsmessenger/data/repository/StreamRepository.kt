package ru.nordbird.tfsmessenger.data.repository

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import ru.nordbird.tfsmessenger.data.api.ZulipService
import ru.nordbird.tfsmessenger.data.dao.AppDatabase
import ru.nordbird.tfsmessenger.data.mapper.StreamDbToStreamMapper
import ru.nordbird.tfsmessenger.data.mapper.StreamNwToStreamDbMapper
import ru.nordbird.tfsmessenger.data.mapper.TopicDbToTopicMapper
import ru.nordbird.tfsmessenger.data.mapper.TopicNwToTopicDbMapper
import ru.nordbird.tfsmessenger.data.model.*

class StreamRepository(
    private val apiService: ZulipService,
    private val dbService: AppDatabase
) {

    private val nwStreamMapper = StreamNwToStreamDbMapper()
    private val dbStreamMapper = StreamDbToStreamMapper()

    private val nwTopicMapper = TopicNwToTopicDbMapper()
    private val dbTopicMapper = TopicDbToTopicMapper()

    fun getStreams(query: String = ""): Flowable<List<Stream>> {
        return Single.concat(
            getDatabaseStreams(query),
            getNetworkStreams(query)
        )
            .map { dbStreamMapper.transform(it) }
    }

    fun getSubscriptions(query: String = ""): Flowable<List<Stream>> {
        return Single.concat(
            getDatabaseSubscriptions(query),
            getNetworkSubscriptions(query)
        )
            .map { dbStreamMapper.transform(it) }
    }

    fun getStreamTopics(streamId: Int): Flowable<List<Topic>> {
        return Single.concat(
            getDatabaseStreamTopics(streamId),
            getNetworkStreamTopics(streamId)
        )
            .map { dbTopicMapper.transform(it) }
    }

    private fun getNetworkStreams(query: String = ""): Single<List<StreamDb>> {
        return apiService.getStreams()
            .map { response ->
                response.streams
                    .map { nwStreamMapper.transform(it) }
            }
            .doOnSuccess { saveStreamsToDatabase(it) }
            .map { streams -> streams.filter { it.name.contains(query, true) } }
    }

    private fun getDatabaseStreams(query: String = ""): Single<List<StreamDb>> {
        return dbService.streamDao().getStreams(query)
    }

    private fun getNetworkSubscriptions(query: String = ""): Single<List<StreamDb>> {
        return apiService.getSubscriptions()
            .map { response ->
                response.subscriptions
                    .map { nwStreamMapper.transform(it) }
                    .onEach { it.subscribed = true }
            }
            .doOnSuccess { saveStreamsToDatabase(it) }
            .map { streams -> streams.filter { it.name.contains(query, true) } }
    }

    private fun getDatabaseSubscriptions(query: String = ""): Single<List<StreamDb>> {
        return dbService.streamDao().getSubscriptions(query)
    }

    private fun getNetworkStreamTopics(streamId: Int): Single<List<TopicDb>> {
        return apiService.getStreamTopics(streamId)
            .flatMapObservable { response ->
                Observable.fromIterable(response.topics
                    .map { nwTopicMapper.transform(it) })
            }
            .map { topic ->
                topic.streamId = streamId
                topic
            }
            .toList()
            .doOnSuccess { saveTopicsToDatabase(it) }
    }

    private fun getDatabaseStreamTopics(streamId: Int): Single<List<TopicDb>> {
        return dbService.topicDao().getByStreamId(streamId)
    }

    private fun saveStreamsToDatabase(streams: List<StreamDb>) {
        dbService.streamDao().insertAll(streams)
    }

    private fun saveTopicsToDatabase(topics: List<TopicDb>) {
        dbService.topicDao().insertAll(topics)
    }
}