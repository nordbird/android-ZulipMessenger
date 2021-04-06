package ru.nordbird.tfsmessenger.data.repository

import ru.nordbird.tfsmessenger.data.DataGenerator
import ru.nordbird.tfsmessenger.data.model.User

object MessageRepository {

    private val messages = DataGenerator.getRandomMessages()

    fun getMessages() = messages

    fun addMessage(user: User, text: String) = DataGenerator.addMessage(user, text)

    fun updateReaction(messageId: String, userId: String, reactionCode: String) = DataGenerator.updateReaction(messageId, userId, reactionCode)

}