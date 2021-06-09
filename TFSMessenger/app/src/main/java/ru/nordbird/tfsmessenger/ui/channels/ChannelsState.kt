package ru.nordbird.tfsmessenger.ui.channels

import ru.nordbird.tfsmessenger.data.model.UnreadCounter
import ru.nordbird.tfsmessenger.ui.channels.base.ChannelsAction
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
    val unreadCounters: List<UnreadCounter> = emptyList(),
    val needScroll: Boolean = false
)

internal fun ChannelsState.reduce(channelsAction: ChannelsAction): ChannelsState {
    return when (channelsAction) {
        ChannelsAction.LoadStreams, ChannelsAction.LoadSubscriptions -> {
            val list = if (streams.isEmpty()) listOf(StreamShimmerUi(), StreamShimmerUi(), StreamShimmerUi()) else items
            copy(
                items = list,
                needScroll = false
            )
        }

        is ChannelsAction.StreamsLoaded -> copy(
            streams = channelsAction.streams,
            items = combineItems(channelsAction.streams, topics, unreadCounters, filterQuery)
        )

        is ChannelsAction.LoadStreamsStop -> {
            val list = if (streams.isEmpty()) listOf(ErrorUi()) else items
            copy(
                items = list
            )
        }

        is ChannelsAction.FilterStreams -> copy(
            filterQuery = channelsAction.query
        )

        is ChannelsAction.StreamsFiltered -> copy(
            items = combineItems(streams, topics, unreadCounters, filterQuery),
            needScroll = true
        )

        ChannelsAction.FilterStreamsStop -> this

        is ChannelsAction.ExpandTopics -> copy(
            needScroll = false
        )

        is ChannelsAction.TopicsLoaded -> {
            val list = channelsAction.topics + topics
            val topicList = list.distinctBy { it.uid }
            copy(
                topics = topicList,
                items = combineItems(streams, topicList, unreadCounters, filterQuery),
                needScroll = false
            )
        }

        is ChannelsAction.CollapseTopics -> {
            val topicList = topics.filterNot { it.streamName == channelsAction.streamName }
            copy(
                topics = topicList,
                items = combineItems(streams, topicList, unreadCounters, filterQuery),
                needScroll = false
            )
        }

        is ChannelsAction.TopicUnreadMessagesLoaded -> {
            val list = listOf(channelsAction.unreadMessageCounter) + unreadCounters
            val counterList = list.distinctBy { "${it.streamName}${it.topicName}" }
            copy(
                unreadCounters = counterList,
                items = combineItems(streams, topics, counterList, filterQuery),
                needScroll = false
            )
        }

        ChannelsAction.LoadTopicUnreadMessagesStop -> this
        is ChannelsAction.CreateStream, ChannelsAction.StreamCreated -> this
    }
}

private fun combineItems(
    streams: List<StreamUi>,
    topics: List<TopicUi>,
    counters: List<UnreadCounter>,
    filterQuery: String
): List<ViewTyped> {
    return streams.filter { it.name.contains(filterQuery, true) }.sortedBy { it.name }.flatMap { stream ->
        val topicList = topics.filter { it.streamName == stream.name }.sortedBy { it.name }.map { topic ->
            val counter = counters.firstOrNull { it.streamName == topic.streamName && it.topicName == topic.name }
            if (counter != null) TopicUi(topic.name, topic.streamName, topic.colorType, counter.count) else topic
        }
        val newStream = StreamUi(stream.id, stream.name, topicList.isNotEmpty())
        listOf(newStream) + topicList
    }
}