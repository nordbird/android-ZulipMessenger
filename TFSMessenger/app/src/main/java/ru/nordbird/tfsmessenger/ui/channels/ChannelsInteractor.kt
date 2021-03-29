package ru.nordbird.tfsmessenger.ui.channels

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import ru.nordbird.tfsmessenger.data.mapper.StreamToStreamUiMapper
import ru.nordbird.tfsmessenger.data.mapper.TopicToTopicUiMapper
import ru.nordbird.tfsmessenger.data.model.Resource
import ru.nordbird.tfsmessenger.data.model.Stream
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

    private var allStreamsTopics: BehaviorSubject<Resource<List<ViewTyped>>> = BehaviorSubject.create()
    private var subscribedStreamsTopics: BehaviorSubject<Resource<List<ViewTyped>>> = BehaviorSubject.create()

    private var allStreams = Resource.loading<List<StreamUi>>()
    private var subscribedStreams = Resource.loading<List<StreamUi>>()

    private val allTopics = mutableMapOf<String, List<TopicUi>>()
    private val subscribedTopics = mutableMapOf<String, List<TopicUi>>()

    private val allStreamIds = mutableListOf<String>()
    private val subscribedStreamIds = mutableListOf<String>()

    private var filterQueryAllStreams = ""
    private var filterQuerySubscribedStreams = ""

    init {
        allStreamsTopics.onNext(Resource.loading())
        subscribedStreamsTopics.onNext(Resource.loading())
        loadAllStreams()
        loadSubscribedStreams()
    }

    fun loadAllStreams() {
        streamRepository.getAllStreams().flatMap { transformStreams(it) }
            .doOnNext { allStreams = it }
            .flatMap { filterStreams(it, filterQueryAllStreams) }
            .flatMap { makeStreamWithTopics(it, allStreamIds, allTopics) }
            .subscribe { allStreamsTopics.onNext(it) }
            .dispose()
    }

    fun loadSubscribedStreams() {
        streamRepository.getSubscribedStreams().flatMap { transformStreams(it) }
            .doOnNext { subscribedStreams = it }
            .flatMap { filterStreams(it, filterQuerySubscribedStreams) }
            .flatMap { makeStreamWithTopics(it, subscribedStreamIds, subscribedTopics) }
            .subscribe { subscribedStreamsTopics.onNext(it) }
            .dispose()
    }

    fun filterAllStreams(searchObservable: Observable<String>): Disposable {
        val (disposable, query) = filterStreams(searchObservable, allStreams, allStreamIds, allTopics, allStreamsTopics)
        filterQueryAllStreams = query
        return disposable
    }

    fun filterSubscribedStreams(searchObservable: Observable<String>): Disposable {
        val (disposable, query) = filterStreams(searchObservable, subscribedStreams, subscribedStreamIds, subscribedTopics, subscribedStreamsTopics)
        filterQuerySubscribedStreams = query
        return disposable
    }

    fun getAllStreams() = allStreamsTopics
    fun getSubscribedStreams() = subscribedStreamsTopics

    fun updateAllStreamTopics(streamId: String) {
        updateStreamTopics(streamId, allStreams, allStreamIds, allTopics, filterQueryAllStreams, allStreamsTopics)
    }

    fun updateSubscribedStreamTopics(streamId: String) {
        updateStreamTopics(streamId, subscribedStreams, subscribedStreamIds, subscribedTopics, filterQuerySubscribedStreams, subscribedStreamsTopics)
    }

    fun getAllStreamsTopic(itemId: String): TopicUi? = getTopic(allTopics, itemId)
    fun getSubscribedStreamsTopic(itemId: String): TopicUi? = getTopic(subscribedTopics, itemId)

    fun getAllStream(streamId: String): StreamUi? = getStream(allStreams.data, streamId)
    fun getSubscribedStream(streamId: String): StreamUi? = getStream(subscribedStreams.data, streamId)

    private fun getStream(streamList: List<StreamUi>?, streamId: String): StreamUi? {
        return streamList?.firstOrNull { it.id == streamId }
    }

    private fun getTopic(topics: MutableMap<String, List<TopicUi>>, itemId: String): TopicUi? {
        return topics.flatMap { it.value }.firstOrNull { it.uid == itemId }
    }

    private fun transformStreams(resource: Resource<List<Stream>>): Observable<Resource<List<StreamUi>>> {
        return Observable.zip(
            Observable.just(resource.status),
            streamMapper.transform(resource.data ?: emptyList())
        ) { status, data -> Resource(status, data) }
    }

    private fun filterStreams(
        searchObservable: Observable<String>,
        streams: Resource<List<StreamUi>>,
        streamIds: MutableList<String>,
        topics: MutableMap<String, List<TopicUi>>,
        streamsTopics: BehaviorSubject<Resource<List<ViewTyped>>>
    ): Pair<Disposable, String> {
        var filterQuery = ""
        return searchObservable
            .map { query -> query.toLowerCase(Locale.getDefault()).trim() }
            .distinctUntilChanged()
            .debounce(500, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .switchMap { query -> Observable.just(query) }
            .doOnNext { filterQuery = it }
            .flatMap { filterStreams(streams, it) }
            .flatMap { makeStreamWithTopics(it, streamIds, topics) }
            .subscribe { streamsTopics.onNext(it) } to filterQuery
    }

    private fun filterStreams(resource: Resource<List<StreamUi>>, query: String): Observable<Resource<List<StreamUi>>> {
        return Observable.just(resource.copy(data = resource.data?.filter { it.name.contains(query, true) }))
    }

    private fun updateStreamTopics(
        streamId: String,
        streams: Resource<List<StreamUi>>,
        streamIds: MutableList<String>,
        topics: MutableMap<String, List<TopicUi>>,
        query: String,
        streamsTopics: BehaviorSubject<Resource<List<ViewTyped>>>
    ) {
        if (streamIds.contains(streamId)) streamIds.remove(streamId)
        else streamIds.add(streamId)

        Observable.fromArray(streams)
            .flatMap { filterStreams(it, query) }
            .flatMap { makeStreamWithTopics(it, streamIds, topics) }
            .subscribe { streamsTopics.onNext(it) }.dispose()
    }

    private fun makeStreamWithTopics(
        resource: Resource<List<StreamUi>>,
        streamIds: MutableList<String>,
        topics: MutableMap<String, List<TopicUi>>
    ): Observable<Resource<List<ViewTyped>>> {
        val newList = mutableListOf<ViewTyped>()
        return Observable.zip(
            Observable.just(resource.status),
            Observable.fromIterable(resource.data ?: emptyList()).concatMap { transformTopics(it, streamIds, topics) }
                .collectInto(
                    newList,
                    { totalList, streamList -> totalList.addAll(streamList) }
                ).toObservable()
        ) { status, data -> Resource(status, data) }
    }

    private fun transformTopics(stream: StreamUi, streamIds: MutableList<String>, topics: MutableMap<String, List<TopicUi>>): Observable<List<ViewTyped>> {
        return Observable.zip(
            Observable.just(stream),
            getStreamTopics(stream.id, streamIds, topics)
        ) { str, t -> listOf(str) + t }
    }

    private fun getStreamTopics(streamId: String, streamIds: MutableList<String>, topics: MutableMap<String, List<TopicUi>>): Observable<List<TopicUi>> {
        return if (streamIds.contains(streamId)) {
            streamRepository.getStreamTopics(streamId).flatMap { topicMapper.transform(it) }.doOnNext { topics[streamId] = it }
        } else {
            Observable.just(emptyList())
        }
    }
}