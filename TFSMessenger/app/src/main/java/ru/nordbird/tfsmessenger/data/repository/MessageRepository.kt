package ru.nordbird.tfsmessenger.data.repository

import ru.nordbird.tfsmessenger.data.DataGenerator
import ru.nordbird.tfsmessenger.data.model.Message
import ru.nordbird.tfsmessenger.data.model.Reaction
import ru.nordbird.tfsmessenger.data.model.User

object MessageRepository {

    private val messages = mutableListOf<Message>()

    init {
        messages.addAll(DataGenerator.getRandomMessages(5))
    }

    fun getMessages() = messages

    fun addMessage(user: User, text: String) {
        val message = DataGenerator.makeMessage(user, text)
        messages.add(0, message)
        messages.add(0, DataGenerator.getRandomIncomingMessage())
    }

    fun updateReaction(messageId: String, userId: String, reactionCode: Int) {
        val message = messages.firstOrNull { it.id == messageId } ?: return
        val reactions = message.reactions.toMutableList()

        val reaction = reactions.firstOrNull { it.code == reactionCode && it.userId == userId }
        if (reaction != null) {
            reactions.remove(reaction)
        } else {
            reactions.add(Reaction(reactionCode, userId))
        }

        message.reactions = reactions
    }

}