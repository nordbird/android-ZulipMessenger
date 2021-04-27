package ru.nordbird.tfsmessenger.ui.channels

import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped
import ru.nordbird.tfsmessenger.ui.recycler.holder.ErrorUi
import ru.nordbird.tfsmessenger.ui.recycler.holder.StreamShimmerUi
import ru.nordbird.tfsmessenger.ui.recycler.holder.StreamUi
import ru.nordbird.tfsmessenger.ui.recycler.holder.TopicUi

data class ChannelsState(
    val filterQuery: String = "",
    val items: List<ViewTyped> = listOf(StreamShimmerUi(), StreamShimmerUi(), StreamShimmerUi()),
    val streams: List<StreamUi> = emptyList(),
    val topics: List<TopicUi> = emptyList(),
    val error: Throwable? = null
)

internal fun ChannelsState.reduce(channelsAction: ChannelsAction): ChannelsState {
    return when (channelsAction) {
        ChannelsAction.LoadStreams -> {
            val list = if (streams.isEmpty()) listOf(StreamShimmerUi(), StreamShimmerUi(), StreamShimmerUi()) else items
            copy(
                items = list,
                error = null
            )
        }

        is ChannelsAction.StreamsLoaded -> copy(
            streams = channelsAction.streams,
            items = combineItems(channelsAction.streams, topics, filterQuery),
            error = null
        )

        is ChannelsAction.ErrorLoadStreams -> {
            val list = if (streams.isEmpty()) listOf(ErrorUi()) else items
            copy(
                items = list,
                error = channelsAction.error
            )
        }

        is ChannelsAction.SearchStreams -> copy(
            filterQuery = channelsAction.query
        )

        ChannelsAction.SearchStreamsStop -> this

        is ChannelsAction.ExpandTopics -> copy(
            error = null
        )

        is ChannelsAction.TopicsLoaded -> {
            val list = topics + channelsAction.topics
            val topicList = list.distinctBy { it.uid }
            copy(
                topics = topicList,
                items = combineItems(streams, topicList, filterQuery),
                error = null
            )
        }

        is ChannelsAction.CollapseTopics -> {
            val topicList = topics.filterNot { it.streamId == channelsAction.streamId }
            copy(
                topics = topicList,
                items = combineItems(streams, topicList, filterQuery),
                error = null
            )
        }
    }
}

private fun combineItems(
    streams: List<StreamUi>,
    topics: List<TopicUi>,
    filterQuery: String
): List<ViewTyped> {
    return streams.filter { it.name.contains(filterQuery, true) }.flatMap { stream ->
        val topicList = topics.filter { it.streamId == stream.id }
        val newStream = StreamUi(stream.id, stream.name, topicList.isNotEmpty())

        listOf(newStream) + topicList
    }
}