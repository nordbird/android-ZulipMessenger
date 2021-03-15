package ru.nordbird.tfsmessenger.data.model

import java.util.*

abstract class BaseMessage(
        val id: String,
        val type: MessageType,
        val author: String,
        val isIncoming: Boolean = true,
        val date: Date = Date(),
        var isReaded: Boolean = false
) {

    abstract fun data(): String

    companion object AbstractFactory {

        private var lastId = -1

        fun makeMessage(
                author: String,
                date: Date = Date(),
                type: MessageType,
                payload: Any?,
                isIncoming: Boolean = false
        ): BaseMessage {
            lastId++

            return when (type) {
                MessageType.IMAGE -> ImageMessage("$lastId", author, isIncoming, date, image = payload as String)
                MessageType.TEXT -> TextMessage("$lastId", author, isIncoming, date, text = payload as String)
            }
        }
    }
}

enum class MessageType {
    TEXT,
    IMAGE
}