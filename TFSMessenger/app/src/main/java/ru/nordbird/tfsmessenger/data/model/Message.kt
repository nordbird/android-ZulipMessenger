package ru.nordbird.tfsmessenger.data.model

data class Message(
    val id: Int,
    val authorId: Int,
    val authorName: String,
    val avatar_url: String,
    val content: String,
    val timestamp_ms: Long,
    val reactions: List<Reaction> = listOf(),
    val localId: Int = 0,
    val link: String = ""
)