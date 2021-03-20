package ru.nordbird.tfsmessenger.data.mapper

import ru.nordbird.tfsmessenger.data.model.Message
import ru.nordbird.tfsmessenger.extensions.toZeroTime
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped
import ru.nordbird.tfsmessenger.ui.recycler.holder.MessageInUi
import ru.nordbird.tfsmessenger.ui.recycler.holder.MessageOutUi
import ru.nordbird.tfsmessenger.ui.recycler.holder.SeparatorDateUi

class MessageToViewTypedMapper : Mapper<List<Message>, List<ViewTyped>> {

    private val reactionMapper = ReactionToReactionGroupMapper()

    override fun transform(data: List<Message>): List<ViewTyped> {
        val messageByDate = data.groupBy { it.date.toZeroTime() }
        return messageByDate.flatMap { (date, list) -> makeMessages(list) + SeparatorDateUi(date) }
    }

    private fun makeMessages(messages: List<Message>): List<ViewTyped> {
        return messages.map {
            if (it.isIncoming) {
                MessageInUi(it.id, it.author, it.content, reactionMapper.transform(it.reactions))
            } else {
                MessageOutUi(it.id, it.author, it.content, reactionMapper.transform(it.reactions))
            }
        }
    }
}