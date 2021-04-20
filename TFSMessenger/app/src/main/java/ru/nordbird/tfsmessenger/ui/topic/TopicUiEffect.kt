package ru.nordbird.tfsmessenger.ui.topic

import java.io.InputStream

sealed class TopicUiEffect{

    class DownloadFile(val stream: InputStream) : TopicUiEffect()
}