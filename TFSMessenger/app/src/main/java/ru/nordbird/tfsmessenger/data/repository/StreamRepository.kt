package ru.nordbird.tfsmessenger.data.repository

import io.reactivex.Flowable
import io.reactivex.Single
import ru.nordbird.tfsmessenger.data.api.ZulipService
import ru.nordbird.tfsmessenger.data.dao.StreamDao
import ru.nordbird.tfsmessenger.data.mapper.StreamDbToStreamMapper
import ru.nordbird.tfsmessenger.data.mapper.StreamNwToStreamDbMapper
import ru.nordbird.tfsmessenger.data.model.*

class StreamRepository(
    private val apiService: ZulipService,
    private val streamDao: StreamDao
) {

    private val nwStreamMapper = StreamNwToStreamDbMapper()
    private val dbStreamMapper = StreamDbToStreamMapper()

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
        return apiService.getStreams()
            .map { response ->
                response.streams
                    .map { nwStreamMapper.transform(it) }
            }
            .doOnSuccess { saveStreamsToDatabase(it) }
            .map { streams -> streams.filter { it.name.contains(query, true) } }
    }

    private fun getDatabaseStreams(query: String = ""): Single<List<StreamDb>> {
        return streamDao.getStreams(query)
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
        return streamDao.getSubscriptions(query)
    }

    private fun saveStreamsToDatabase(streams: List<StreamDb>) {
        streamDao.insertAll(streams)
    }

}