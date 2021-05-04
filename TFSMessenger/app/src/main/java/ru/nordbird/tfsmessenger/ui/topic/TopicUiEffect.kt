package ru.nordbird.tfsmessenger.ui.topic

import java.io.InputStream

sealed class TopicUiEffect{

    class ActionError(val error: Throwable): TopicUiEffect()

    class DownloadFile(val stream: InputStream) : TopicUiEffect()
}