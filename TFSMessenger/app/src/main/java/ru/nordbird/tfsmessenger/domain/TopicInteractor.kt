package ru.nordbird.tfsmessenger.domain

import io.reactivex.Flowable
import io.reactivex.Single
import ru.nordbird.tfsmessenger.data.api.ZulipAuth
import ru.nordbird.tfsmessenger.data.emojiSet.EMOJI_SET
import ru.nordbird.tfsmessenger.data.emojiSet.Emoji
import ru.nordbird.tfsmessenger.data.mapper.MessageToViewTypedMapper
import ru.nordbird.tfsmessenger.data.model.Message
import ru.nordbird.tfsmessenger.data.repository.MessageRepository
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped
import ru.nordbird.tfsmessenger.ui.recycler.holder.MessageUi
import java.io.InputStream

class TopicInteractor(
    private val messageRepository: MessageRepository
) {

    companion object {
        private const val COUNT_MESSAGES_PER_REQUEST = 20
    }

    private val messageMapper = MessageToViewTypedMapper(ZulipAuth.AUTH_ID)

    private val items = mutableListOf<Message>()

    fun loadMessages(streamName: String, topicName: String, messageId: Int): Flowable<List<ViewTyped>> {
        return messageRepository.getMessages(streamName, topicName, messageId, COUNT_MESSAGES_PER_REQUEST)
            .map { transformMessages(it) }
    }

    fun addMessage(streamName: String, topicName: String, text: String): Flowable<List<ViewTyped>> {
        return messageRepository.addMessage(streamName, topicName, ZulipAuth.AUTH_ID, text)
            .map { transformMessages(it) }
    }

    fun updateReaction(message: MessageUi, currentUserId: Int, reactionCode: String): Flowable<List<ViewTyped>> {
        val msg = items.first { it.id == message.id }
        val baseReaction = EMOJI_SET.firstOrNull { it.getCodeString() == reactionCode } ?: Emoji("", "", 0)
        val reaction = message.reactions.firstOrNull { it.userIdList.contains(currentUserId) }

        return if (reaction != null) {
            if (reaction.name != baseReaction.name) {
                Flowable.concat(
                    messageRepository.removeReaction(msg, currentUserId, reaction.name),
                    messageRepository.addReaction(msg, currentUserId, baseReaction.code, baseReaction.name)
                )
            } else
                messageRepository.removeReaction(msg, currentUserId, reaction.name)
        } else {
            messageRepository.addReaction(msg, currentUserId, baseReaction.code, baseReaction.name)
        }
            .map { transformMessages(it) }
    }

    fun sendFile(streamName: String, topicName: String, name: String, stream: InputStream?): Flowable<List<ViewTyped>> {
        return messageRepository.sendFile(streamName, topicName, ZulipAuth.AUTH_ID, name, stream)
            .map { transformMessages(it) }
    }

    fun downloadFile(url: String): Single<InputStream> {
        return messageRepository.downloadFile(url)
    }

    private fun transformMessages(newList: List<Message>): List<ViewTyped> {
        val oldList = items.filterNot { messageExists(newList, it) }
        items.clear()
        items.addAll(oldList)
        items.addAll(newList)
        items.sortBy { it.id }
        return messageMapper.transform(items)
    }

    private fun messageExists(list: List<Message>, message: Message): Boolean {
        return list.firstOrNull { it.id == message.id || (it.localId != 0 && it.localId == message.localId) } != null
    }
}