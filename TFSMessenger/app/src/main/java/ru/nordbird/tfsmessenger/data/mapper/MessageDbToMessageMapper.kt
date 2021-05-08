package ru.nordbird.tfsmessenger.data.mapper

import ru.nordbird.tfsmessenger.data.mapper.base.Mapper
import ru.nordbird.tfsmessenger.data.model.Attachment
import ru.nordbird.tfsmessenger.data.model.Message
import ru.nordbird.tfsmessenger.data.model.MessageDb
import java.util.regex.Matcher
import java.util.regex.Pattern

class MessageDbToMessageMapper(
    private val baseUrl: String,
    private val uploadPath: String
) : Mapper<List<MessageDb>, List<Message>> {

    override fun transform(data: List<MessageDb>): List<Message> {
        return data.map {
            val (content, attachments) = getAttachments(it.content)
            Message(it.id, it.topicName, it.authorId, it.authorName, it.avatar_url, content, it.timestamp_ms, it.reactions, it.localId, attachments)
        }
    }

    private fun getAttachments(content: String): Pair<String, List<Attachment>> {
        var text: String = content
        val result: MutableList<Attachment> = mutableListOf()
        val pattern: Pattern = Pattern.compile("\\[.+?]\\(.+?\\)")
        val matcher: Matcher = pattern.matcher(content)

        while (matcher.find()) {
            val attachment = content.substring(matcher.start(), matcher.end())
            val title = attachment.substringBeforeLast("](").drop(1)
            val url = attachment.substringAfterLast("]").dropLast(1)
                .replace("($uploadPath", "($baseUrl/$uploadPath")
                .replace("(/$uploadPath", "($baseUrl/$uploadPath")
                .drop(1)

            text = text.replace(attachment, title)
            result.add(Attachment(title, url))
        }

        return text to result
    }

}