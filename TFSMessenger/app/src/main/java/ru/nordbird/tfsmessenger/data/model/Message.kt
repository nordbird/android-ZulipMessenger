package ru.nordbird.tfsmessenger.data.model

data class Message(
    val id: Int,
    val authorId: Int,
    val authorName: String,
    val avatar_url: String,
    val content: String,
    val timestamp_ms: Long,
    var reactions: List<Reaction> = listOf(),
    var isIncoming: Boolean = false,
    val localId: Int = 0
)