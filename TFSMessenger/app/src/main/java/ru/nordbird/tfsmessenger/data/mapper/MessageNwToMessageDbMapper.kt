package ru.nordbird.tfsmessenger.data.mapper

import ru.nordbird.tfsmessenger.data.model.MessageDb
import ru.nordbird.tfsmessenger.data.model.MessageNw
import ru.nordbird.tfsmessenger.extensions.SECOND

class MessageNwToMessageDbMapper : Mapper<List<MessageNw>, List<MessageDb>> {

    override fun transform(data: List<MessageNw>): List<MessageDb> {
        return data.map { MessageDb(it.id, "", "", it.authorId, it.authorName, it.avatar_url, it.content, it.timestamp_sec * SECOND, it.reactions) }
    }

}