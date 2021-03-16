package ru.nordbird.tfsmessenger.data.repository

import ru.nordbird.tfsmessenger.data.DataGenerator
import ru.nordbird.tfsmessenger.data.model.BaseMessage
import ru.nordbird.tfsmessenger.data.model.MessageType

object MessageRepository {

    private val messages = mutableListOf<BaseMessage>()

    init {
        messages.addAll(DataGenerator.getRandomMessages(5))
    }

    fun getMessages() = messages

    fun addMessage(text: String) {
        val message = BaseMessage.makeMessage("", type = MessageType.TEXT, payload = text)
        messages.add(0, message)
        messages.add(0, DataGenerator.getRandomIncomingMessage())
    }

    fun addReaction(uid: String, code: Int) {
        messages.filter { it.id == uid }
    }
}