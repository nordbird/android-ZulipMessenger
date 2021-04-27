package ru.nordbird.tfsmessenger.ui.topic

import ru.nordbird.tfsmessenger.ui.recycler.holder.MessageUi
import java.io.InputStream

sealed class TopicAction {

    data class FirstLoadMessages(val streamName: String, val topicName: String) : TopicAction()

    data class NextLoadMessages(val streamName: String, val topicName: String) : TopicAction()

    data class MessagesLoaded(val newMessages: List<MessageUi>) : TopicAction()

    data class ErrorLoadMessages(val error: Throwable) : TopicAction()

    data class SendMessage(val streamName: String, val topicName: String, val content: String) : TopicAction()

    data class UpdateReaction(val message: MessageUi, val currentUserId: Int, val reactionCode: String) : TopicAction()

    data class SendFile(val streamName: String, val topicName: String, val name: String, val stream: InputStream?) : TopicAction()

    data class DownloadFile(val url: String) : TopicAction()

    data class FileDownloaded(val stream: InputStream) : TopicAction()
}
