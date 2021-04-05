package ru.nordbird.tfsmessenger.data.repository

import io.reactivex.Single
import ru.nordbird.tfsmessenger.data.api.ZulipServiceImpl
import ru.nordbird.tfsmessenger.data.model.Stream
import ru.nordbird.tfsmessenger.data.model.Topic

object StreamRepository {

    fun getStreams(): Single<List<Stream>> = ZulipServiceImpl.getApi().getStreams().map { it.streams }

    fun getSubscriptions(): Single<List<Stream>> = ZulipServiceImpl.getApi().getSubscriptions().map { it.subscriptions }

    fun getStreamTopics(streamId: String): Single<List<Topic>> = ZulipServiceImpl.getApi().getStreamTopics(streamId).map { it.topics }

}