package ru.nordbird.tfsmessenger.ui.channels

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import ru.nordbird.tfsmessenger.data.mapper.StreamToStreamUiMapper
import ru.nordbird.tfsmessenger.data.mapper.TopicToTopicUiMapper
import ru.nordbird.tfsmessenger.data.model.Stream
import ru.nordbird.tfsmessenger.data.model.Topic
import ru.nordbird.tfsmessenger.data.repository.StreamRepository
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped
import ru.nordbird.tfsmessenger.ui.recycler.holder.StreamUi
import ru.nordbird.tfsmessenger.ui.recycler.holder.TopicUi
import java.util.*
import java.util.concurrent.TimeUnit

object ChannelsInteractor {

    private val streamRepository = StreamRepository
    private val streamMapper = StreamToStreamUiMapper()
    private val topicMapper = TopicToTopicUiMapper()

    private var allStreams: BehaviorSubject<List<ViewTyped>> = BehaviorSubject.create()
    private val subscribedStreams: BehaviorSubject<List<ViewTyped>> = BehaviorSubject.create()

    private val allTopics = mutableMapOf<String, List<Topic>>()
    private val subscribedTopics = mutableMapOf<String, List<Topic>>()

    fun filterAllStreams(searchObservable: Observable<String>) {
        searchObservable.filter { it.isNotBlank() }
            .map { it.toLowerCase(Locale.getDefault()).trim() }
            .distinctUntilChanged()
            .debounce(500, TimeUnit.MILLISECONDS)
            .subscribe { query -> updateAllStreams(query) }
    }

    private fun updateAllStreams(query: String) {
        val streamList = streamRepository.getAllStreams().map { streams -> filterStreams(streams, query) }
        streamList.flatMap { streamMapper.transform(it) }.flatMap { streams -> Observable.fromIterable(streams) }
            .flatMap { makeStreamWithTopics(it, allTopics) }.subscribe { allStreams.onNext(it) }
    }

    fun getAllStreams() = allStreams
    fun getSubscribedStreams() = subscribedStreams

    private fun filterStreams(streams: List<Stream>, query: String): List<Stream> {
        return streams.filter { it.name.contains(query, true) }
    }

    fun updateSubscribedStreams(query: String) {
        val streamList = streamRepository.getAllStreams().map { streams -> filterStreams(streams, query) }
        streamList.flatMap { streamMapper.transform(it) }.flatMap { streams -> Observable.fromIterable(streams) }
            .flatMap { makeStreamWithTopics(it, allTopics) }.subscribe { allStreams.onNext(it) }
    }

    fun updateAllStreamTopics(streamId: String) {
        allStreams.map { updateStreamTopics(it, allTopics, streamId) }
    }

    fun updateSubscribedStreamTopics(streamId: String) {
        subscribedStreams.map { updateStreamTopics(it, subscribedTopics, streamId) }
    }

    fun getAllStreamsTopic(itemId: String): Observable<TopicUi> {
        return allStreams.flatMapMaybe { getTopic(it, itemId) }
    }

    fun getSubscribedStreamsTopic(itemId: String): Observable<TopicUi> {
        return subscribedStreams.flatMapMaybe { getTopic(it, itemId) }
    }

    fun getAllStream(streamId: String): Observable<StreamUi> {
        return allStreams.flatMapMaybe { getStream(it, streamId) }
    }

    fun getSubscribedStream(streamId: String): Observable<StreamUi> {
        return subscribedStreams.flatMapMaybe { getStream(it, streamId) }
    }

    private fun getStream(streamList: List<ViewTyped>, streamId: String): Maybe<StreamUi> {
        val stream = streamList.filterIsInstance<StreamUi>().firstOrNull { it.id == streamId }
        return Maybe.just(stream)
    }

    private fun getTopic(streamList: List<ViewTyped>, itemId: String): Maybe<TopicUi> {
        val topic = streamList.filterIsInstance<TopicUi>().firstOrNull { it.uid == itemId }
        return Maybe.just(topic)
    }

    private fun updateStreamTopics(
        streamList: List<ViewTyped>,
        topicMap: MutableMap<String, List<Topic>>,
        streamId: String
    ) {
        val stream = streamList.filterIsInstance<StreamUi>().firstOrNull { it.id == streamId } ?: return

        if (!stream.topicExpanded) {
            streamRepository.getStreamTopics(streamId).subscribe { topicMap[streamId] = it }
        } else {
            topicMap.remove(streamId)
        }
    }

    private fun makeStreamWithTopics(stream: StreamUi, topicMap: Map<String, List<Topic>>): Observable<List<ViewTyped>> {
        val list = if (topicMap.containsKey(stream.id)) {
            stream.topicExpanded = true
            val topicList = topicMap[stream.id]!!
            listOf(stream) + topicMapper.transform(topicList)
        } else listOf(stream)

        return Observable.just(list)
    }

}