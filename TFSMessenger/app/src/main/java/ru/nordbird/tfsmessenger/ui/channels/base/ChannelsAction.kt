package ru.nordbird.tfsmessenger.ui.channels.base

import ru.nordbird.tfsmessenger.data.model.UnreadCounter
import ru.nordbird.tfsmessenger.ui.recycler.holder.StreamUi
import ru.nordbird.tfsmessenger.ui.recycler.holder.TopicUi

sealed class ChannelsAction {

    object LoadStreams : ChannelsAction()

    data class StreamsLoaded(val streams: List<StreamUi>) : ChannelsAction()

    object LoadStreamsStop : ChannelsAction()

    data class FilterStreams(val query: String) : ChannelsAction()

    object StreamsFiltered : ChannelsAction()

    object FilterStreamsStop : ChannelsAction()

    data class ExpandTopics(val streamId: Int, val streamName: String) : ChannelsAction()

    data class CollapseTopics(val streamName: String) : ChannelsAction()

    data class TopicsLoaded(val topics: List<TopicUi>) : ChannelsAction()

    data class TopicUnreadMessagesLoaded(val unreadMessageCounter: UnreadCounter) : ChannelsAction()

    object LoadTopicUnreadMessagesStop : ChannelsAction()
}