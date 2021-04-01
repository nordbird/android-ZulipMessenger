package ru.nordbird.tfsmessenger.ui.channels

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import ru.nordbird.tfsmessenger.data.mapper.StreamToStreamUiMapper
import ru.nordbird.tfsmessenger.data.mapper.TopicToTopicUiMapper
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

    private var allStreamsTopics: BehaviorSubject<List<ViewTyped>> = BehaviorSubject.create()
    private var subscribedStreamsTopics: BehaviorSubject<List<ViewTyped>> = BehaviorSubject.create()

    private var allStreams = emptyList<StreamUi>()
    private var subscribedStreams = emptyList<StreamUi>()

    private val allTopics = mutableMapOf<String, List<TopicUi>>()
    private val subscribedTopics = mutableMapOf<String, List<TopicUi>>()

    private val allStreamIds = mutableListOf<String>()
    private val subscribedStreamIds = mutableListOf<String>()

    private var filterQueryAllStreams = ""
    private var filterQuerySubscribedStreams = ""

    private val compositeDisposable = CompositeDisposable()

    init {
        allStreamsTopics.onNext(emptyList())
        subscribedStreamsTopics.onNext(emptyList())
//        compositeDisposable.add(loadAllStreams())
//        compositeDisposable.add(loadSubscribedStreams())
    }

    fun clearDisposable() {
        compositeDisposable.clear()
    }

    fun loadAllStreams(): Disposable {
        return streamRepository.getAllStreams()
            .flatMap { streamMapper.transform(it) }
            .doOnNext { allStreams = it }
            .map { filterStreams(it, filterQueryAllStreams) }
            .flatMap { makeStreamWithTopics(it, allStreamIds, allTopics) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { allStreamsTopics.onNext(it) }
    }

    fun loadSubscribedStreams(): Disposable {
        return streamRepository.getSubscribedStreams()

            .flatMap { streamMapper.transform(it) }
            .doOnNext { subscribedStreams = it }
            .map { filterStreams(it, filterQuerySubscribedStreams) }
            .flatMap { makeStreamWithTopics(it, subscribedStreamIds, subscribedTopics) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { subscribedStreamsTopics.onNext(it) }
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

    fun getAllStream(streamId: String): StreamUi? = getStream(allStreams, streamId)
    fun getSubscribedStream(streamId: String): StreamUi? = getStream(subscribedStreams, streamId)

    private fun getStream(streamList: List<StreamUi>?, streamId: String): StreamUi? {
        return streamList?.firstOrNull { it.id == streamId }
    }

    private fun getTopic(topics: MutableMap<String, List<TopicUi>>, itemId: String): TopicUi? {
        return topics.flatMap { it.value }.firstOrNull { it.uid == itemId }
    }

    private fun filterStreams(
        searchObservable: Observable<String>,
        streams: List<StreamUi>,
        streamIds: MutableList<String>,
        topics: MutableMap<String, List<TopicUi>>,
        streamsTopics: BehaviorSubject<List<ViewTyped>>
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
            .map { filterStreams(streams, it) }
            .flatMap { makeStreamWithTopics(it, streamIds, topics) }
            .subscribe { streamsTopics.onNext(it) } to filterQuery
    }

    private fun filterStreams(resource: List<StreamUi>, query: String): List<StreamUi> {
        return resource.filter { it.name.contains(query, true) }
    }

    private fun updateStreamTopics(
        streamId: String,
        streams: List<StreamUi>,
        streamIds: MutableList<String>,
        topics: MutableMap<String, List<TopicUi>>,
        query: String,
        streamsTopics: BehaviorSubject<List<ViewTyped>>
    ) {
        if (streamIds.contains(streamId)) streamIds.remove(streamId)
        else streamIds.add(streamId)

        Observable.fromArray(streams)
            .map { filterStreams(it, query) }
            .flatMap { makeStreamWithTopics(it, streamIds, topics) }
            .subscribe { streamsTopics.onNext(it) }.dispose()
    }

    private fun makeStreamWithTopics(
        resource: List<StreamUi>,
        streamIds: MutableList<String>,
        topics: MutableMap<String, List<TopicUi>>
    ): Observable<List<ViewTyped>> {
        val newList = mutableListOf<ViewTyped>()
        return Observable.fromIterable(resource).concatMap { transformTopics(it, streamIds, topics) }
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