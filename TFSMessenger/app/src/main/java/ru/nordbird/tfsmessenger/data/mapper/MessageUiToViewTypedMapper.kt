package ru.nordbird.tfsmessenger.data.mapper

import ru.nordbird.tfsmessenger.data.mapper.base.Mapper
import ru.nordbird.tfsmessenger.extensions.toZeroTime
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped
import ru.nordbird.tfsmessenger.ui.recycler.holder.MessageInUi
import ru.nordbird.tfsmessenger.ui.recycler.holder.MessageUi
import ru.nordbird.tfsmessenger.ui.recycler.holder.SeparatorDateUi
import java.util.*

class MessageUiToViewTypedMapper : Mapper<List<MessageUi>, List<ViewTyped>> {

    private val attachmentInMapper = AttachmentToAttachmentUiMapper(true)
    private val attachmentOutMapper = AttachmentToAttachmentUiMapper(false)

    override fun transform(data: List<MessageUi>): List<ViewTyped> {
        val messageByDate = data.asReversed().groupBy { Date(it.timestamp_ms).toZeroTime() }
        return messageByDate.flatMap { (date, messages) -> makeMessagesWithAttachments(messages) + SeparatorDateUi(date) }
    }

    private fun makeMessagesWithAttachments(messages: List<MessageUi>): List<ViewTyped> {
        return messages.flatMap { message ->
            if (message is MessageInUi) {
                attachmentInMapper.transform(message.attachments) + message
            } else {
                attachmentOutMapper.transform(message.attachments) + message
            }
        }
    }
}