package ru.nordbird.tfsmessenger.ui.topic.base

import java.io.InputStream

sealed class TopicUiEffect{

    class ActionError(val error: Throwable): TopicUiEffect()

    class DownloadFile(val stream: InputStream) : TopicUiEffect()

    class TopicsLoaded(val topics: List<String>) : TopicUiEffect()
}