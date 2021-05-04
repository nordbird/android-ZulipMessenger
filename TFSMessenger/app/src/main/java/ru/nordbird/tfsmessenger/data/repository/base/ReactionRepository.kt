package ru.nordbird.tfsmessenger.data.repository.base

import io.reactivex.Flowable
import ru.nordbird.tfsmessenger.data.model.Message

interface ReactionRepository {

    fun addReaction(messageId: Int, currentUserId: Int, reactionCode: Int, reactionName: String): Flowable<List<Message>>

    fun removeReaction(messageId: Int, currentUserId: Int, reactionCode: Int, reactionName: String): Flowable<List<Message>>

}