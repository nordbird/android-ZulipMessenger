package ru.nordbird.tfsmessenger.domain

import io.reactivex.Flowable
import io.reactivex.Single
import ru.nordbird.tfsmessenger.data.api.ZulipAuth
import ru.nordbird.tfsmessenger.data.emojiSet.EMOJI_SET
import ru.nordbird.tfsmessenger.data.emojiSet.Emoji
import ru.nordbird.tfsmessenger.data.mapper.MessageToMessageUiMapper
import ru.nordbird.tfsmessenger.data.model.Message
import ru.nordbird.tfsmessenger.data.repository.MessageRepository
import ru.nordbird.tfsmessenger.ui.recycler.holder.MessageUi
import java.io.InputStream

class TopicInteractor(
    private val messageRepository: MessageRepository
) {

    companion object {
        private const val COUNT_MESSAGES_PER_REQUEST = 20
    }

    private val messageMapper = MessageToMessageUiMapper(ZulipAuth.AUTH_ID)

    fun loadMessages(streamName: String, topicName: String, messageId: Int): Flowable<List<MessageUi>> {
        return messageRepository.getMessages(streamName, topicName, messageId, COUNT_MESSAGES_PER_REQUEST)
            .map { messageMapper.transform(it) }
    }

    fun addMessage(streamName: String, topicName: String, text: String): Flowable<List<MessageUi>> {
        return messageRepository.addMessage(streamName, topicName, ZulipAuth.AUTH_ID, text)
            .map { messageMapper.transform(it) }
    }

    fun updateReaction(message: MessageUi, currentUserId: Int, reactionCode: String): Flowable<List<MessageUi>> {
        val clickedReaction = EMOJI_SET.firstOrNull { it.getCodeString() == reactionCode } ?: Emoji("", "", 0)
        val selectedReactionName = message.reactions.firstOrNull { it.userIdList.contains(currentUserId) }?.name

        return if (selectedReactionName != null) {
            val selectedReaction = EMOJI_SET.firstOrNull { it.name == selectedReactionName } ?: Emoji("", "", 0)

            if (selectedReaction.name != clickedReaction.name)
                replaceReaction(message.id, currentUserId, selectedReaction.code, selectedReaction.name)
            else
                removeReaction(message.id, currentUserId, selectedReaction.code, selectedReaction.name)
        } else {
            addReaction(message.id, currentUserId, clickedReaction.code, clickedReaction.name)
        }
            .map { messageMapper.transform(it) }
    }

    fun sendFile(streamName: String, topicName: String, name: String, stream: InputStream?): Flowable<List<MessageUi>> {
        return messageRepository.sendFile(streamName, topicName, ZulipAuth.AUTH_ID, name, stream)
            .map { messageMapper.transform(it) }
    }

    fun downloadFile(url: String): Single<InputStream> {
        return messageRepository.downloadFile(url)
    }

    private fun addReaction(messageId: Int, currentUserId: Int, reactionCode: Int, reactionName: String): Flowable<List<Message>> {
        return messageRepository.addReaction(messageId, currentUserId, reactionCode, reactionName)
    }

    private fun removeReaction(messageId: Int, currentUserId: Int, reactionCode: Int, reactionName: String): Flowable<List<Message>> {
        return messageRepository.removeReaction(messageId, currentUserId, reactionCode, reactionName)
    }

    private fun replaceReaction(messageId: Int, currentUserId: Int, reactionCode: Int, reactionName: String): Flowable<List<Message>> {
        return Flowable.concat(
            removeReaction(messageId, currentUserId, reactionCode, reactionName),
            addReaction(messageId, currentUserId, reactionCode, reactionName)
        )
    }
}