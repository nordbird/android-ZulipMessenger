package ru.nordbird.tfsmessenger.data.model

import java.util.*

abstract class BaseMessage(
        val id: String,
        val type: MessageType,
        val author: User,
        val isIncoming: Boolean = true,
        val date: Date = Date(),
        var isReaded: Boolean = false,
        var reactions: List<Reaction> = listOf()
) {

    abstract fun data(): String

    companion object AbstractFactory {

        private var lastId = 0

        fun makeMessage(
                author: User,
                date: Date = Date(),
                type: MessageType,
                payload: Any?,
                isIncoming: Boolean = false,
                reactions: List<Reaction>
        ): BaseMessage {
            lastId++

            return when (type) {
                MessageType.IMAGE -> ImageMessage("$lastId", author, isIncoming, date, reactions = reactions, image = payload as String)
                MessageType.TEXT -> TextMessage("$lastId", author, isIncoming, date, reactions = reactions, text = payload as String)
            }
        }
    }
}

enum class MessageType {
    TEXT,
    IMAGE
}