package ru.nordbird.tfsmessenger.data.mapper

import ru.nordbird.tfsmessenger.data.model.BaseMessage
import ru.nordbird.tfsmessenger.data.model.MessageType
import ru.nordbird.tfsmessenger.extensions.toZeroTime
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped
import ru.nordbird.tfsmessenger.ui.recycler.holder.MessageInUi
import ru.nordbird.tfsmessenger.ui.recycler.holder.MessageOutUi
import ru.nordbird.tfsmessenger.ui.recycler.holder.SeparatorDateUi

class BaseMessageToMessageUi : (List<BaseMessage>) -> List<ViewTyped> {

    override fun invoke(messages: List<BaseMessage>): List<ViewTyped> {
        val messageByDate = messages.groupBy { it.date.toZeroTime() }
        return messageByDate.flatMap { (date, list) -> makeMessages(list) + SeparatorDateUi(date) }
    }

    private fun makeMessages(messages: List<BaseMessage>): List<ViewTyped> {
        return messages.map {
            when (it.type) {
                MessageType.TEXT -> if (it.isIncoming) MessageInUi(it.id, it.author, it.data()) else MessageOutUi(it.id, it.data())
                MessageType.IMAGE -> if (it.isIncoming) MessageInUi(it.id, it.author, it.data()) else MessageOutUi(it.id, it.data())
            }
        }
    }

}