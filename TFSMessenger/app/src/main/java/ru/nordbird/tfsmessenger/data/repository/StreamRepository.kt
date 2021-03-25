package ru.nordbird.tfsmessenger.data.repository

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import ru.nordbird.tfsmessenger.data.DataGenerator
import ru.nordbird.tfsmessenger.data.model.Stream

object StreamRepository {

    private val allStreams = BehaviorSubject.create<List<Stream>>()
    private val subscribedStreams = BehaviorSubject.create<List<Stream>>()

    init {
        allStreams.onNext(DataGenerator.getAllStreams())
        subscribedStreams.onNext(DataGenerator.getSubscribedStreams())
    }

    fun getAllStreams() = allStreams

    fun getSubscribedStreams() = subscribedStreams

    fun getStreamTopics(streamId: String) = Observable.just(DataGenerator.getTopics(streamId))

}