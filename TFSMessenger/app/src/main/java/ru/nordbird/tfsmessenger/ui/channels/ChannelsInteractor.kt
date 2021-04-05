package ru.nordbird.tfsmessenger.ui.channels

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.nordbird.tfsmessenger.data.mapper.StreamToStreamUiMapper
import ru.nordbird.tfsmessenger.data.mapper.TopicToTopicUiMapper
import ru.nordbird.tfsmessenger.data.model.Stream
import ru.nordbird.tfsmessenger.data.repository.StreamRepository
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped
import ru.nordbird.tfsmessenger.ui.recycler.holder.StreamUi
import ru.nordbird.tfsmessenger.ui.recycler.holder.TopicUi
import java.util.*
import java.util.concurrent.TimeUnit

class ChannelsInteractor(private val tabType: ChannelsTabType) {

    private val streamRepository = StreamRepository
    private val streamMapper = StreamToStreamUiMapper()
    private val topicMapper = TopicToTopicUiMapper()

    private val streamIds = mutableListOf<String>()

    fun getStreams(): Single<List<ViewTyped>> =
        getStreamsFun()
            .map { streams -> streamMapper.transform(streams) }
            .flatMap { makeStreamWithTopics(it) }
            .subscribeOn(Schedulers.io())

    fun filterStreams(searchObservable: Observable<String>): Observable<List<ViewTyped>> {
        return searchObservable
            .map { query -> query.toLowerCase(Locale.getDefault()).trim() }
            .distinctUntilChanged()
            .debounce(500, TimeUnit.MILLISECONDS)
            .switchMap { query ->
                getStreamsFun()
                    .map { streams -> streamMapper.transform(streams) }
                    .map { list -> list.filter { it.name.toLowerCase(Locale.getDefault()).contains(query) } }
                    .flatMap { makeStreamWithTopics(it) }
                    .toObservable()
            }
            .onErrorReturnItem(emptyList())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun updateStreamTopics(streamId: String): Single<List<ViewTyped>> {
        if (streamIds.contains(streamId)) {
            streamIds.remove(streamId)
        } else {
            streamIds.add(streamId)
        }

        return getStreams()
    }

    private fun makeStreamWithTopics(streams: List<StreamUi>): Single<List<ViewTyped>> {
        return Observable.fromIterable(streams)
            .flatMap(
                { stream ->
                    if (streamIds.contains(stream.id))
                        streamRepository.getStreamTopics(stream.id)
                            .map { topicMapper.transform(it) }
                            .toObservable()
                    else
                        Observable.fromArray(emptyList())
                },
                { stream, topics -> transformTopics(stream, topics) }
            ).reduce { listA, listB -> listA + listB }.toSingle()
    }

    private fun transformTopics(stream: StreamUi, topics: List<TopicUi>): List<ViewTyped> {
        return listOf(stream) + topics.onEach { it.streamId = stream.id }
    }

    private fun getStreamsFun(): Single<List<Stream>> {
        return when (tabType) {
            ChannelsTabType.ALL -> streamRepository.getStreams()
            ChannelsTabType.SUBSCRIBED -> streamRepository.getSubscriptions()
        }
    }
}