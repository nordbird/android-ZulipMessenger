package ru.nordbird.tfsmessenger.data.repository

import io.reactivex.Flowable
import io.reactivex.Single
import ru.nordbird.tfsmessenger.data.api.ZulipAuth
import ru.nordbird.tfsmessenger.data.api.ZulipService
import ru.nordbird.tfsmessenger.data.dao.MessageDao
import ru.nordbird.tfsmessenger.data.mapper.MessageDbToMessageMapper
import ru.nordbird.tfsmessenger.data.model.*
import ru.nordbird.tfsmessenger.data.repository.base.ReactionRepository

class ReactionRepositoryImpl(
    private val apiService: ZulipService,
    private val messageDao: MessageDao
) : ReactionRepository {
    private val dbMessageMapper = MessageDbToMessageMapper(ZulipAuth.BASE_URL)

    override fun addReaction(messageId: Int, currentUserId: Int, reactionCode: Int, reactionName: String): Flowable<List<Message>> {
        return Single.concat(
            addDatabaseReaction(messageId, currentUserId, reactionCode, reactionName),
            apiService.addMessageReaction(messageId, reactionName).flatMap { response ->
                if (response.result == RESPONSE_RESULT_SUCCESS) {
                    messageDao.getById(messageId)
                } else {
                    removeDatabaseReaction(messageId, currentUserId, reactionName)
                }
            }
        )
            .map { dbMessageMapper.transform(listOf(it)) }
            .onErrorReturnItem(emptyList())
    }

    override fun removeReaction(messageId: Int, currentUserId: Int, reactionCode: Int, reactionName: String): Flowable<List<Message>> {
        return Single.concat(
            removeDatabaseReaction(messageId, currentUserId, reactionName),
            apiService.removeMessageReaction(messageId, reactionName).flatMap { response ->
                if (response.result == RESPONSE_RESULT_SUCCESS) {
                    messageDao.getById(messageId)
                } else {
                    addDatabaseReaction(messageId, currentUserId, reactionCode, reactionName)
                }
            }
        )
            .map { dbMessageMapper.transform(listOf(it)) }
            .onErrorReturnItem(emptyList())
    }

    private fun addDatabaseReaction(messageId: Int, senderId: Int, reactionCode: Int, reactionName: String): Single<MessageDb> {
        return messageDao.getById(messageId).map { message ->
            val list = message.reactions.toMutableList()
            list.add(Reaction(reactionCode.toString(16), reactionName, senderId))
            saveToDatabase(message.copy(reactions = list))
        }
    }

    private fun removeDatabaseReaction(messageId: Int, senderId: Int, reactionName: String): Single<MessageDb> {
        return messageDao.getById(messageId).map { message ->
            val list = message.reactions.filterNot { it.userId == senderId && it.name == reactionName }
            saveToDatabase(message.copy(reactions = list))
        }
    }

    private fun saveToDatabase(message: MessageDb): MessageDb {
        messageDao.insert(message)
        return message
    }
}