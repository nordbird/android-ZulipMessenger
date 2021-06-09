package ru.nordbird.tfsmessenger.ui.topic.base

import java.io.InputStream

sealed class TopicUiEffect {

    class ActionError(val error: Throwable) : TopicUiEffect()

    class FileDownloaded(val fileName: String, val stream: InputStream) : TopicUiEffect()

    class TopicsLoaded(val topics: List<String>) : TopicUiEffect()

    class MessageLoaded(val content: String) : TopicUiEffect()

    object MessageUpdated : TopicUiEffect()
}