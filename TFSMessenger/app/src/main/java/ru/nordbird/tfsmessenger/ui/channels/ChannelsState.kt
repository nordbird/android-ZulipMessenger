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
    val needScroll: Boolean = false
)

internal fun ChannelsState.reduce(channelsAction: ChannelsAction): ChannelsState {
    return when (channelsAction) {
        ChannelsAction.LoadStreams -> {
            val list = if (streams.isEmpty()) listOf(StreamShimmerUi(), StreamShimmerUi(), StreamShimmerUi()) else items
            copy(
                items = list,
                error = null,
                needScroll = false
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

        is ChannelsAction.StreamsFound -> copy(
            streams = channelsAction.streams,
            items = combineItems(channelsAction.streams, topics, filterQuery),
            error = null,
            needScroll = true
        )

        ChannelsAction.SearchStreamsStop -> this

        is ChannelsAction.ExpandTopics -> copy(
            error = null,
            needScroll = false
        )

        is ChannelsAction.TopicsLoaded -> {
            val list = topics + channelsAction.topics
            val topicList = list.distinctBy { it.uid }
            copy(
                topics = topicList,
                items = combineItems(streams, topicList, filterQuery),
                error = null,
                needScroll = false
            )
        }

        is ChannelsAction.CollapseTopics -> {
            val topicList = topics.filterNot { it.streamName == channelsAction.streamName }
            copy(
                topics = topicList,
                items = combineItems(streams, topicList, filterQuery),
                error = null,
                needScroll = false
            )
        }

        is ChannelsAction.TopicUnreadMessagesLoaded -> {
            val topic = topics.firstOrNull {
                it.name == channelsAction.topicName && it.streamName == channelsAction.streamName
            }?.let {
                TopicUi(it.name, it.color, channelsAction.unreadMessageCount, it.streamName)
            }

            val topicList = if (topic != null) (listOf(topic) + topics).distinctBy { it.uid } else topics

            copy(
                topics = topicList,
                items = combineItems(streams, topicList, filterQuery),
                error = null,
                needScroll = false
            )
        }

        ChannelsAction.LoadTopicUnreadMessagesStop -> this
    }
}

private fun combineItems(
    streams: List<StreamUi>,
    topics: List<TopicUi>,
    filterQuery: String
): List<ViewTyped> {
    return streams.filter { it.name.contains(filterQuery, true) }.flatMap { stream ->
        val topicList = topics.filter { it.streamName == stream.name }.sortedBy { it.name }
        val newStream = StreamUi(stream.id, stream.name, topicList.isNotEmpty())

        listOf(newStream) + topicList
    }
}