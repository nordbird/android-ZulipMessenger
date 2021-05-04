package ru.nordbird.tfsmessenger.domain

import io.reactivex.Flowable
import io.reactivex.Single
import ru.nordbird.tfsmessenger.data.api.ZulipAuth
import ru.nordbird.tfsmessenger.data.emojiSet.EMOJI_SET
import ru.nordbird.tfsmessenger.data.emojiSet.Emoji
import ru.nordbird.tfsmessenger.data.mapper.MessageToMessageUiMapper
import ru.nordbird.tfsmessenger.data.model.Message
import ru.nordbird.tfsmessenger.data.repository.base.MessageRepository
import ru.nordbird.tfsmessenger.data.repository.base.ReactionRepository
import ru.nordbird.tfsmessenger.domain.base.TopicInteractor
import ru.nordbird.tfsmessenger.ui.recycler.holder.MessageUi
import java.io.InputStream

class TopicInteractorImpl(
    private val messageRepository: MessageRepository,
    private val reactionRepository: ReactionRepository
) : TopicInteractor {

    companion object {
        private const val COUNT_MESSAGES_PER_REQUEST = 20
    }

    private val messageMapper = MessageToMessageUiMapper(ZulipAuth.AUTH_ID)

    override fun loadMessages(streamName: String, topicName: String, messageId: Int): Flowable<List<MessageUi>> {
        return messageRepository.getMessages(streamName, topicName, messageId, COUNT_MESSAGES_PER_REQUEST)
            .map { messageMapper.transform(it) }
    }

    override fun addMessage(streamName: String, topicName: String, text: String): Flowable<List<MessageUi>> {
        return messageRepository.addMessage(streamName, topicName, ZulipAuth.AUTH_ID, text)
            .map { messageMapper.transform(it) }
    }

    override fun updateReaction(message: MessageUi, currentUserId: Int, reactionCode: String): Flowable<List<MessageUi>> {
        val clickedReaction = EMOJI_SET.firstOrNull { it.getCodeString() == reactionCode } ?: Emoji("", "", 0)
        val isSelectedReaction = message.reactions.firstOrNull { it.userIdList.contains(currentUserId) && it.name == clickedReaction.name } != null

        return if (isSelectedReaction) {
            removeReaction(message.id, currentUserId, clickedReaction.code, clickedReaction.name)
        } else {
            addReaction(message.id, currentUserId, clickedReaction.code, clickedReaction.name)
        }
            .map { messageMapper.transform(it) }
    }

    override fun sendFile(streamName: String, topicName: String, name: String, stream: InputStream?): Flowable<List<MessageUi>> {
        return messageRepository.sendFile(streamName, topicName, ZulipAuth.AUTH_ID, name, stream)
            .map { messageMapper.transform(it) }
    }

    override fun downloadFile(url: String): Single<InputStream> {
        return messageRepository.downloadFile(url)
    }

    private fun addReaction(messageId: Int, currentUserId: Int, reactionCode: Int, reactionName: String): Flowable<List<Message>> {
        return reactionRepository.addReaction(messageId, currentUserId, reactionCode, reactionName)
    }

    private fun removeReaction(messageId: Int, currentUserId: Int, reactionCode: Int, reactionName: String): Flowable<List<Message>> {
        return reactionRepository.removeReaction(messageId, currentUserId, reactionCode, reactionName)
    }
}