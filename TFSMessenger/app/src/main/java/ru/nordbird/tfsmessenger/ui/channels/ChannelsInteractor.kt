package ru.nordbird.tfsmessenger.ui.channels

import io.reactivex.Flowable
import io.reactivex.Observable
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

    fun getStreams(query: String = ""): Flowable<List<ViewTyped>> {
        return getStreamsFun(query)
            .observeOn(Schedulers.computation())
            .map { streamMapper.transform(it) }
            .map { streams -> streams.sortedBy { it.name } }
            .flatMap { makeStreamWithTopics(it) }
            .subscribeOn(Schedulers.io())
    }

    fun filterStreams(searchObservable: Observable<String>): Observable<List<ViewTyped>> {
        return searchObservable
            .map { query -> query.toLowerCase(Locale.getDefault()).trim() }
            .distinctUntilChanged()
            .debounce(500, TimeUnit.MILLISECONDS)
            .switchMap { query -> getStreams(query).toObservable() }
            .onErrorReturnItem(emptyList())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun updateStreamTopics(streamId: String): Flowable<List<ViewTyped>> {
        if (streamIds.contains(streamId)) {
            streamIds.remove(streamId)
        } else {
            streamIds.add(streamId)
        }

        return getStreams()
    }

    private fun makeStreamWithTopics(streams: List<StreamUi>): Flowable<List<ViewTyped>> {
        return Flowable.fromIterable(streams)
            .flatMap(
                { stream ->
                    if (streamIds.contains(stream.id))
                        streamRepository.getStreamTopics(stream.id)
                            .map { topicMapper.transform(it) }
                    else
                        Flowable.fromArray(emptyList())
                },
                { stream, topics -> transformTopics(stream, topics) }
            )
            .distinct { it.firstOrNull()?.uid }
            .reduce { listA, listB -> listA + listB }
            .toFlowable()
    }

    private fun transformTopics(stream: StreamUi, topics: List<TopicUi>): List<ViewTyped> {
        return listOf(stream) + topics
    }

    private fun getStreamsFun(query: String = ""): Flowable<List<Stream>> {
        return when (tabType) {
            ChannelsTabType.ALL -> streamRepository.getStreams(query)
            ChannelsTabType.SUBSCRIBED -> streamRepository.getSubscriptions(query)
        }
    }
}