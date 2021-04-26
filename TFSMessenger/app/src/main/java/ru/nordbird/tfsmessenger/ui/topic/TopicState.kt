package ru.nordbird.tfsmessenger.ui.topic

import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped
import ru.nordbird.tfsmessenger.ui.recycler.holder.*

data class TopicState(
    val oldestMessageId: Int = Int.MAX_VALUE,
    val items: List<ViewTyped> = listOf(TopicShimmerUi(), TopicShimmerUi()),
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
            val minId = minOf(oldestMessageId, topicAction.messages.filterIsInstance<MessageUi>().minOfOrNull { it.id } ?: oldestMessageId)
            copy(
                oldestMessageId = minId,
                items = topicAction.messages,
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