package ru.nordbird.tfsmessenger.ui.topic

import ru.nordbird.tfsmessenger.data.mapper.MessageUiToViewTypedMapper
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped
import ru.nordbird.tfsmessenger.ui.recycler.holder.*
import ru.nordbird.tfsmessenger.ui.topic.base.TopicAction

data class TopicState(
    val streamName: String = "",
    val topicName: String = "",
    val oldestMessageId: Int = Int.MAX_VALUE,
    val items: List<ViewTyped> = listOf(TopicShimmerUi(), TopicShimmerUi()),
    val messages: List<MessageUi> = emptyList(),
    val needScroll: Boolean = false,
    val queueId: String = ""
)

internal fun TopicState.reduce(topicAction: TopicAction): TopicState {
    return when (topicAction) {
        is TopicAction.FirstLoadMessages -> {
            val topicChanged = (topicAction.streamName != streamName || topicAction.topicName != topicName)
            copy(
                streamName = topicAction.streamName,
                topicName = topicAction.topicName,
                oldestMessageId = if (topicChanged) Int.MAX_VALUE else oldestMessageId,
                messages = if (topicChanged) emptyList() else messages,
                needScroll = topicChanged
            )
        }

        is TopicAction.NextLoadMessages -> copy(needScroll = false)

        is TopicAction.MessagesLoaded -> {
            val mapper = MessageUiToViewTypedMapper(topicName.isNotEmpty())
            val list = combineMessages(messages, topicAction.newMessages)
            val minId = minOf(oldestMessageId, topicAction.newMessages.minOfOrNull { it.id } ?: oldestMessageId)
            copy(
                oldestMessageId = minId,
                items = mapper.transform(list),
                messages = list
            )
        }

        is TopicAction.MessagesUpdated -> {
            val mapper = MessageUiToViewTypedMapper(topicName.isNotEmpty())
            val list = combineMessages(messages, topicAction.newMessages)
            val minId = minOf(oldestMessageId, topicAction.newMessages.minOfOrNull { it.id } ?: oldestMessageId)
            copy(
                oldestMessageId = minId,
                items = mapper.transform(list),
                messages = list,
                needScroll = false
            )
        }

        TopicAction.LoadMessagesStop, TopicAction.EventQueueStop -> this

        is TopicAction.SendMessage -> copy(needScroll = true)

        is TopicAction.UpdateReaction -> copy(needScroll = false)

        is TopicAction.SendFile -> copy(needScroll = true)
        is TopicAction.DownloadFile -> this
        is TopicAction.FileDownloaded -> this

        is TopicAction.RegisterEventQueue -> this
        is TopicAction.EventQueueRegistered -> copy(queueId = topicAction.queueId)
        TopicAction.DeleteEventQueue -> copy(queueId = "")
    }
}

private fun combineMessages(oldMessages: List<MessageUi>, newMessages: List<MessageUi>): List<MessageUi> {
    val oldList = oldMessages.filterNot { messageExists(newMessages, it) }
    val newList = mutableListOf<MessageUi>()

    newList.addAll(oldList)
    newList.addAll(newMessages)
    newList.sortBy { it.id }

    return newList
}

private fun messageExists(list: List<MessageUi>, message: MessageUi): Boolean {
    return list.firstOrNull { it.id == message.id || (it.localId != 0 && it.localId == message.localId) } != null
}