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
    val error: Throwable? = null,
    val isLoading: Boolean = false,
)

private fun combineItems(
    streams: List<StreamUi>,
    topics: List<TopicUi>
): List<ViewTyped> {
    return streams.flatMap { stream ->
        val topicList = topics.filter { it.streamId == stream.id }
        val newStream = StreamUi(stream.id, stream.name, topicList.isNotEmpty())

        listOf(newStream) + topicList
    }
}

internal fun ChannelsState.reduce(channelsAction: ChannelsAction): ChannelsState {
    return when (channelsAction) {
        ChannelsAction.LoadStreams -> copy(
            items = listOf(StreamShimmerUi(), StreamShimmerUi(), StreamShimmerUi()),
            isLoading = true,
            error = null
        )

        is ChannelsAction.StreamsLoaded -> copy(
            streams = channelsAction.streams,
            items = combineItems(channelsAction.streams, topics),
            isLoading = false
        )

        is ChannelsAction.ErrorLoadStreams -> copy(
            items = listOf(ErrorUi()),
            error = channelsAction.error,
            isLoading = false
        )

        is ChannelsAction.SearchStreams -> copy(
            filterQuery = channelsAction.query
        )

        ChannelsAction.SearchStreamsStop -> copy(
            isLoading = false
        )

        is ChannelsAction.ExpandTopics -> this

        is ChannelsAction.TopicsLoaded -> {
            val list = topics + channelsAction.topics
            val topicList = list.distinctBy { it.uid }
            copy(
                topics = topicList,
                items = combineItems(streams, topicList)
            )
        }

        is ChannelsAction.CollapseTopics -> {
            val topicList = topics.filterNot { it.streamId == channelsAction.streamId }
            copy(
                topics = topicList,
                items = combineItems(streams, topicList)
            )
        }
    }
}
