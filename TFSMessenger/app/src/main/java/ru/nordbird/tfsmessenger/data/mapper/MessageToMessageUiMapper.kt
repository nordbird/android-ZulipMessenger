package ru.nordbird.tfsmessenger.data.mapper

import ru.nordbird.tfsmessenger.data.model.Message
import ru.nordbird.tfsmessenger.ui.recycler.holder.MessageInUi
import ru.nordbird.tfsmessenger.ui.recycler.holder.MessageOutUi
import ru.nordbird.tfsmessenger.ui.recycler.holder.MessageUi

class MessageToMessageUiMapper(
    private val currentUserId: Int
) : Mapper<List<Message>, List<MessageUi>> {

    private val reactionMapper = ReactionToReactionGroupMapper()

    override fun transform(data: List<Message>): List<MessageUi> {
        return data.map {
            val isIncoming = it.authorId != currentUserId

            if (isIncoming) {
                MessageInUi(
                    it.id, it.localId, it.authorId, it.authorName, it.avatar_url,
                    it.content, reactionMapper.transform(it.reactions), it.link, it.timestamp_ms
                )
            } else {
                MessageOutUi(
                    it.id, it.localId, it.authorId,
                    it.content, reactionMapper.transform(it.reactions), it.link, it.timestamp_ms
                )
            }
        }
    }

}