package ru.nordbird.tfsmessenger.data.repository.base

import io.reactivex.Flowable
import io.reactivex.Single
import ru.nordbird.tfsmessenger.data.model.Stream

interface StreamRepository {

    fun getStreams(): Flowable<List<Stream>>

    fun getSubscriptions(): Flowable<List<Stream>>

    fun createStream(streamName: String): Single<Boolean>

}