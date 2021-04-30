package ru.nordbird.tfsmessenger.ui.channels

import ru.nordbird.tfsmessenger.ui.recycler.holder.StreamUi
import ru.nordbird.tfsmessenger.ui.recycler.holder.TopicUi

sealed class ChannelsAction {

    object LoadStreams : ChannelsAction()

    data class StreamsLoaded(val streams: List<StreamUi>) : ChannelsAction()

    data class ErrorLoadStreams(val error: Throwable) : ChannelsAction()

    data class SearchStreams(val query: String) : ChannelsAction()

    data class StreamsFound(val streams: List<StreamUi>) : ChannelsAction()

    object SearchStreamsStop : ChannelsAction()

    data class ExpandTopics(val streamId: Int, val streamName: String) : ChannelsAction()

    data class CollapseTopics(val streamName: String) : ChannelsAction()

    data class TopicsLoaded(val topics: List<TopicUi>) : ChannelsAction()

    data class TopicUnreadMessagesLoaded(val streamName: String, val topicName: String, val unreadMessageCount: Int) : ChannelsAction()

    object LoadTopicUnreadMessagesStop : ChannelsAction()
}