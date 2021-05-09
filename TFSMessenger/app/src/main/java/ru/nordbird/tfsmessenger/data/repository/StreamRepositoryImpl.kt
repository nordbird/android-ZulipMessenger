package ru.nordbird.tfsmessenger.data.repository

import io.reactivex.Flowable
import io.reactivex.Single
import ru.nordbird.tfsmessenger.data.api.ZulipService
import ru.nordbird.tfsmessenger.data.dao.StreamDao
import ru.nordbird.tfsmessenger.data.mapper.StreamDbToStreamMapper
import ru.nordbird.tfsmessenger.data.mapper.StreamNwToStreamDbMapper
import ru.nordbird.tfsmessenger.data.model.*
import ru.nordbird.tfsmessenger.data.repository.base.StreamRepository

class StreamRepositoryImpl(
    private val apiService: ZulipService,
    private val streamDao: StreamDao
) : StreamRepository {

    private val nwStreamMapper = StreamNwToStreamDbMapper()
    private val dbStreamMapper = StreamDbToStreamMapper()

    override fun getStreams(): Flowable<List<Stream>> {
        return Single.concat(
            getDatabaseStreams(),
            getNetworkStreams()
        )
            .map { dbStreamMapper.transform(it) }
    }

    override fun getSubscriptions(): Flowable<List<Stream>> {
        return Single.concat(
            getDatabaseSubscriptions(),
            getNetworkSubscriptions()
        )
            .map { dbStreamMapper.transform(it) }
    }

    private fun getNetworkStreams(): Single<List<StreamDb>> {
        return apiService.getStreams()
            .map { response ->
                nwStreamMapper.transform(response.streams)
            }
            .doOnSuccess { saveStreamsToDatabase(it) }
    }

    private fun getDatabaseStreams(): Single<List<StreamDb>> {
        return streamDao.getStreams()
    }

    private fun getNetworkSubscriptions(): Single<List<StreamDb>> {
        return apiService.getSubscriptions()
            .map { response ->
                nwStreamMapper.transform(response.subscriptions)
                    .map { stream -> stream.copy(subscribed = true) }
            }
            .doOnSuccess { saveSubscriptionsToDatabase(it) }
    }

    private fun getDatabaseSubscriptions(): Single<List<StreamDb>> {
        return streamDao.getSubscriptions()
    }

    private fun saveStreamsToDatabase(streams: List<StreamDb>) {
        streamDao.insertStreams(streams)
    }

    private fun saveSubscriptionsToDatabase(streams: List<StreamDb>) {
        streamDao.insertSubscriptions(streams)
    }
}