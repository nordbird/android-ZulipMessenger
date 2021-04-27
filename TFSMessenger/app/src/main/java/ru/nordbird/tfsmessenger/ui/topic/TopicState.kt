package ru.nordbird.tfsmessenger.ui.topic

import ru.nordbird.tfsmessenger.data.mapper.MessageUiToViewTypedMapper
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped
import ru.nordbird.tfsmessenger.ui.recycler.holder.*

data class TopicState(
    val oldestMessageId: Int = Int.MAX_VALUE,
    val items: List<ViewTyped> = listOf(TopicShimmerUi(), TopicShimmerUi()),
    val messages: List<MessageUi> = emptyList(),
    val error: Throwable? = null,
    val isLoading: Boolean = false,
    val needScroll: Boolean = false
)

internal fun TopicState.reduce(topicAction: TopicAction): TopicState {
    return when (topicAction) {
        is TopicAction.FirstLoadMessages -> copy(
            needScroll = true,
            isLoading = true,
            error = null
        )

        is TopicAction.NextLoadMessages -> copy(
            needScroll = false,
            isLoading = true,
            error = null
        )

        is TopicAction.MessagesLoaded -> {
            val oldList = messages.filterNot { messageExists(topicAction.newMessages, it) }
            val newList = mutableListOf<MessageUi>()

            newList.addAll(oldList)
            newList.addAll(topicAction.newMessages)
            newList.sortBy { it.id }

            val mapper = MessageUiToViewTypedMapper()
            val minId = minOf(oldestMessageId, topicAction.newMessages.minOfOrNull { it.id } ?: oldestMessageId)
            copy(
                oldestMessageId = minId,
                items = mapper.transform(newList),
                messages = newList,
                isLoading = false
            )
        }

        is TopicAction.ErrorLoadMessages -> copy(
            error = topicAction.error,
            isLoading = false
        )

        is TopicAction.SendMessage -> copy(
            needScroll = true
        )

        is TopicAction.UpdateReaction -> copy(
            needScroll = false
        )

        is TopicAction.SendFile -> copy(
            needScroll = true
        )

        is TopicAction.DownloadFile -> this

        is TopicAction.FileDownloaded -> this
    }
}

private fun messageExists(list: List<MessageUi>, message: MessageUi): Boolean {
    return list.firstOrNull { it.id == message.id || (it.localId != 0 && it.localId == message.localId) } != null
}