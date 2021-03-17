package ru.nordbird.tfsmessenger.data.model

import java.util.*

class ImageMessage(
        id: String,
        author: User,
        isIncoming: Boolean = false,
        date: Date = Date(),
        isReaded: Boolean = false,
        reactions: List<Reaction>,
        var image: String?
) : BaseMessage(id, MessageType.IMAGE, author, isIncoming, date, isReaded, reactions) {

    override fun data(): String {
        return image.orEmpty()
    }
}