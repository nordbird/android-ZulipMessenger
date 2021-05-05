package ru.nordbird.tfsmessenger.data.model

data class Message(
    val id: Int,
    val authorId: Int,
    val authorName: String,
    val avatar_url: String,
    val content: String,
    val timestamp_ms: Long,
    val reactions: List<Reaction> = emptyList(),
    val localId: Int = 0,
    val attachments: List<Attachment> = emptyList()
)