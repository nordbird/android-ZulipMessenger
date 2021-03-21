package ru.nordbird.tfsmessenger.data.model

import java.util.*

data class Message(
        val id: String,
        val author: User,
        val content: String,
        val isIncoming: Boolean = true,
        val date: Date = Date(),
        var isReaded: Boolean = false,
        var reactions: List<Reaction> = listOf()
)