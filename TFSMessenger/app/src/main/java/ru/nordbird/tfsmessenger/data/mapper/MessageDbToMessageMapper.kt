package ru.nordbird.tfsmessenger.data.mapper

import ru.nordbird.tfsmessenger.data.model.Message
import ru.nordbird.tfsmessenger.data.model.MessageDb

class MessageDbToMessageMapper : Mapper<List<MessageDb>, List<Message>> {

    override fun transform(data: List<MessageDb>): List<Message> {
        return data.map { Message(it.id, it.authorId, it.authorName, it.avatar_url, it.content, it.timestamp_ms, it.reactions) }
    }

}