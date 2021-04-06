package ru.nordbird.tfsmessenger.ui.topic

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.nordbird.tfsmessenger.data.api.ZulipAuth
import ru.nordbird.tfsmessenger.data.mapper.MessageToViewTypedMapper
import ru.nordbird.tfsmessenger.data.model.BaseResponse
import ru.nordbird.tfsmessenger.data.model.Message
import ru.nordbird.tfsmessenger.data.repository.MessageRepository
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped
import ru.nordbird.tfsmessenger.ui.recycler.holder.MessageUi

object TopicInteractor {

    private val messageRepository = MessageRepository
    private val messageMapper = MessageToViewTypedMapper()

    fun getMessages(streamName: String, topicName: String): Single<List<ViewTyped>> = messageRepository.getMessages(streamName, topicName)
        .map { resource -> transformMessages(resource) }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    fun addMessage(streamName: String, topicName: String, text: String): Single<BaseResponse> = messageRepository.addMessage(streamName, topicName, text)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    fun updateReaction(message: MessageUi, currentUserId: String, reactionCode: String): Single<BaseResponse> {
        val reaction = message.reactions.firstOrNull { it.code == reactionCode && it.userIdList.contains(currentUserId) }

        return if (reaction != null) {
            messageRepository.removeReaction(message.id, reactionCode)
        } else {
            messageRepository.addReaction(message.id, reactionCode)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun transformMessages(resource: List<Message>): List<ViewTyped> {
        return messageMapper.transform(resource.onEach { it.isIncoming = it.authorId.toString() != ZulipAuth.AUTH_ID })
    }

}