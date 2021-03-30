package ru.nordbird.tfsmessenger.data.mapper

import io.reactivex.Observable
import ru.nordbird.tfsmessenger.data.model.Message
import ru.nordbird.tfsmessenger.extensions.toZeroTime
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped
import ru.nordbird.tfsmessenger.ui.recycler.holder.MessageInUi
import ru.nordbird.tfsmessenger.ui.recycler.holder.MessageOutUi
import ru.nordbird.tfsmessenger.ui.recycler.holder.SeparatorDateUi

class MessageToViewTypedMapper : Mapper<List<Message>, Observable<List<ViewTyped>>> {

    private val reactionMapper = ReactionToReactionGroupMapper()

    override fun transform(data: List<Message>): Observable<List<ViewTyped>> {
        return Observable.fromArray(data).map { messages ->
            val messageByDate = messages.groupBy { it.date.toZeroTime() }
            return@map messageByDate.flatMap { (date, list) -> makeMessages(list) + SeparatorDateUi(date) }
        }
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