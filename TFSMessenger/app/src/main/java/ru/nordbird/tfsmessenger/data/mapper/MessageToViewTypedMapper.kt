package ru.nordbird.tfsmessenger.data.mapper

import ru.nordbird.tfsmessenger.data.model.Message
import ru.nordbird.tfsmessenger.extensions.toZeroTime
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped
import ru.nordbird.tfsmessenger.ui.recycler.holder.MessageInUi
import ru.nordbird.tfsmessenger.ui.recycler.holder.MessageOutUi
import ru.nordbird.tfsmessenger.ui.recycler.holder.SeparatorDateUi
import java.util.*

class MessageToViewTypedMapper(
    private val currentUserId: Int
) : Mapper<List<Message>, List<ViewTyped>> {

    private val reactionMapper = ReactionToReactionGroupMapper()

    override fun transform(data: List<Message>): List<ViewTyped> {
        val messageByDate = data.asReversed().groupBy { Date(it.timestamp_ms).toZeroTime() }
        return messageByDate.flatMap { (date, list) -> makeMessages(list) + SeparatorDateUi(date) }
    }

    private fun makeMessages(messages: List<Message>): List<ViewTyped> {
        return messages.map {
            val isIncoming = it.authorId != currentUserId

            if (isIncoming) {
                MessageInUi(it.id.toString(), it.authorId, it.authorName, it.avatar_url, it.content, reactionMapper.transform(it.reactions), it.link)
            } else {
                MessageOutUi(it.id.toString(), it.authorId, it.content, reactionMapper.transform(it.reactions), it.link)
            }
        }
    }

}