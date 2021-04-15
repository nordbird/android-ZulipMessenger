package ru.nordbird.tfsmessenger.ui.topic

import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.nordbird.tfsmessenger.data.api.ZulipAuth
import ru.nordbird.tfsmessenger.data.api.ZulipServiceImpl
import ru.nordbird.tfsmessenger.data.emojiSet.EMOJI_SET
import ru.nordbird.tfsmessenger.data.emojiSet.Emoji
import ru.nordbird.tfsmessenger.data.mapper.MessageToViewTypedMapper
import ru.nordbird.tfsmessenger.data.model.Message
import ru.nordbird.tfsmessenger.data.repository.MessageRepository
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped
import ru.nordbird.tfsmessenger.ui.recycler.holder.MessageUi
import java.io.InputStream

class TopicInteractor {

    companion object {
        private const val COUNT_MESSAGES_PER_REQUEST = 10
    }

    private val messageRepository = MessageRepository
    private val messageMapper = MessageToViewTypedMapper()

    private val items = mutableListOf<Message>()
    private var minId = Int.MAX_VALUE

    fun getMessages(streamName: String, topicName: String): Single<List<ViewTyped>> {
        return messageRepository.getMessages(streamName, topicName, minId, COUNT_MESSAGES_PER_REQUEST)
            .map { transformMessages(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun addMessage(streamName: String, topicName: String, text: String): Flowable<List<ViewTyped>> {
        return messageRepository.addMessage(streamName, topicName, ZulipAuth.AUTH_ID, text)
            .map { transformMessages(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun updateReaction(message: MessageUi, currentUserId: String, reactionCode: String): Flowable<List<ViewTyped>> {
        val msg = items.first { it.id.toString() == message.id }
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
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun sendFile(streamName: String, topicName: String, name: String, stream: InputStream?): Flowable<List<ViewTyped>> {
        return messageRepository.sendFile(streamName, topicName, ZulipAuth.AUTH_ID, name, stream)
            .map { transformMessages(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun transformMessages(messages: List<Message>): List<ViewTyped> {
        val newList = messages.map {
            val content = it.content
                .replace("=\"user_uploads/", "=\"${ZulipServiceImpl.BASE_URL}/user_uploads/")
                .replace("=\"/user_uploads/", "=\"${ZulipServiceImpl.BASE_URL}/user_uploads/")
            it.copy(isIncoming = it.authorId.toString() != ZulipAuth.AUTH_ID, content = content)
        }
        newList.onEach { minId = minOf(minId, it.id) }
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