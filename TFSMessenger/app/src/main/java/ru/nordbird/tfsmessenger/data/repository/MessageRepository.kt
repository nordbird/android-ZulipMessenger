package ru.nordbird.tfsmessenger.data.repository

import ru.nordbird.tfsmessenger.data.DataGenerator
import ru.nordbird.tfsmessenger.data.model.BaseMessage
import ru.nordbird.tfsmessenger.data.model.MessageType
import ru.nordbird.tfsmessenger.data.model.Reaction
import ru.nordbird.tfsmessenger.data.model.User

object MessageRepository {

    private val messages = mutableListOf<BaseMessage>()

    init {
        messages.addAll(DataGenerator.getRandomMessages(5))
    }

    fun getMessages() = messages

    fun addMessage(user: User, text: String) {
        val message = BaseMessage.makeMessage(user, type = MessageType.TEXT, payload = text, reactions = mutableListOf())
        messages.add(0, message)
        messages.add(0, DataGenerator.getRandomIncomingMessage())
    }

    fun updateReaction(messageId: String, userId: String, reactionCode: Int) {
        val message = messages.firstOrNull { it.id == messageId } ?: return
        val reactions = message.reactions.toMutableList()

        var reaction = reactions.firstOrNull { it.code == reactionCode }
        if (reaction != null) {
            val index = reactions.indexOf(reaction)
            reactions.remove(reaction)
            val userIdList = reaction.userIdList.toMutableList()
            reaction = Reaction(reactionCode, userIdList)

            if (userIdList.contains(userId)) {
                userIdList.remove(userId)
            } else {
                userIdList.add(userId)
            }
            if (userIdList.size > 0) {
                reactions.add(index, reaction)
            }
        } else {
            reactions.add(Reaction(reactionCode, mutableListOf(userId)))
        }

        message.reactions = reactions
    }

}