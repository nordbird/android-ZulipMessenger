package ru.nordbird.tfsmessenger.data.model

import java.util.*

class TextMessage(
        id: String,
        author: String,
        isIncoming: Boolean = false,
        date: Date = Date(),
        isReaded: Boolean = false,
        var text: String?
) : BaseMessage(id, MessageType.TEXT, author, isIncoming, date, isReaded) {

    override fun data(): String {
        return text.orEmpty()
    }
}