package ru.nordbird.tfsmessenger.data.model

data class UnreadCounter(
    val streamName: String,
    val topicName: String,
    val count: Int
)