package ru.nordbird.tfsmessenger.data.repository

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import ru.nordbird.tfsmessenger.data.api.ZulipServiceImpl
import ru.nordbird.tfsmessenger.data.dao.AppDatabaseImpl
import ru.nordbird.tfsmessenger.data.mapper.StreamDbToStreamMapper
import ru.nordbird.tfsmessenger.data.mapper.StreamNwToStreamDbMapper
import ru.nordbird.tfsmessenger.data.mapper.TopicDbToTopicMapper
import ru.nordbird.tfsmessenger.data.mapper.TopicNwToTopicDbMapper
import ru.nordbird.tfsmessenger.data.model.*

object StreamRepository {

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

    private fun getNetworkStreams(query: String = ""): Single<List<StreamDb>> {
        return ZulipServiceImpl.getApi()
            .getStreams()
            .map { it.streams }
            .map { streams -> streams.map { nwStreamMapper.transform(it) } }
            .doOnSuccess { saveStreamsToDatabase(it) }
            .map { streams -> streams.filter { it.name.contains(query, true) } }
    }

    private fun getDatabaseStreams(query: String = ""): Single<List<StreamDb>> {
        return AppDatabaseImpl.streamDao().getStreams(query)
    }

    private fun getNetworkSubscriptions(query: String = ""): Single<List<StreamDb>> {
        return ZulipServiceImpl.getApi()
            .getSubscriptions()
            .map { it.subscriptions }
            .map { streams -> streams.map { nwStreamMapper.transform(it) } }
            .map { streams -> streams.onEach { it.subscribed = true } }
            .doOnSuccess { saveStreamsToDatabase(it) }
            .map { streams -> streams.filter { it.name.contains(query, true) } }
    }

    private fun getDatabaseSubscriptions(query: String = ""): Single<List<StreamDb>> {
        return AppDatabaseImpl.streamDao().getSubscriptions(query)
    }

    fun getStreamTopics(streamId: String): Flowable<List<Topic>> {
        return Single.concat(
            getDatabaseStreamTopics(streamId),
            getNetworkStreamTopics(streamId)
        )
            .observeOn(Schedulers.computation())
            .map { dbTopicMapper.transform(it) }
    }

    private fun getNetworkStreamTopics(streamId: String): Single<List<TopicDb>> {
        return ZulipServiceImpl.getApi()
            .getStreamTopics(streamId)
            .observeOn(Schedulers.computation())
            .flatMapObservable { Observable.fromIterable(it.topics) }
            .map { nwTopicMapper.transform(it) }
            .map { topic ->
                topic.streamId = streamId
                topic
            }
            .toList()
            .observeOn(Schedulers.io())
            .doOnSuccess { saveTopicsToDatabase(it) }
    }

    private fun getDatabaseStreamTopics(streamId: String): Single<List<TopicDb>> {
        return AppDatabaseImpl.topicDao().getByStreamId(streamId)
    }

    private fun saveStreamsToDatabase(streams: List<StreamDb>) {
        AppDatabaseImpl.streamDao().insertAll(streams)
    }

    private fun saveTopicsToDatabase(topics: List<TopicDb>) {
        AppDatabaseImpl.topicDao().insertAll(topics)
    }
}