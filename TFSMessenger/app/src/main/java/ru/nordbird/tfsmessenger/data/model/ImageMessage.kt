package ru.nordbird.tfsmessenger.data.model

import java.util.*

class ImageMessage(
        id: String,
        author: String,
        isIncoming: Boolean = false,
        date: Date = Date(),
        isReaded: Boolean = false,
        var image: String?
) : BaseMessage(id, MessageType.IMAGE, author, isIncoming, date) {

    override fun data(): String {
        return image.orEmpty()
    }
}