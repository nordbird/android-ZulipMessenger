package ru.nordbird.tfsmessenger.data.model

import java.util.*

class TextMessage(
        id: String,
        author: User,
        isIncoming: Boolean = false,
        date: Date = Date(),
        isReaded: Boolean = false,
        reactions: List<Reaction>,
        var text: String?
) : BaseMessage(id, MessageType.TEXT, author, isIncoming, date, isReaded, reactions) {

    override fun data(): String {
        return text.orEmpty()
    }
}