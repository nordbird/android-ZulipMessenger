package ru.nordbird.tfsmessenger.data.mapper

import android.text.SpannableStringBuilder
import android.text.style.URLSpan
import androidx.core.text.HtmlCompat
import androidx.core.text.toHtml
import ru.nordbird.tfsmessenger.data.model.Message
import ru.nordbird.tfsmessenger.data.model.MessageDb

class MessageDbToMessageMapper(
    private val baseUrl: String
) : Mapper<List<MessageDb>, List<Message>> {

    override fun transform(data: List<MessageDb>): List<Message> {
        return data.map {
            val content = it.content
                .replace("=\"user_uploads/", "=\"$baseUrl/user_uploads/")
                .replace("=\"/user_uploads/", "=\"$baseUrl/user_uploads/")

            // тут конечно можно получить и список ссылок в будущем
            val (text, link) = getLink(content)
            Message(it.id, it.authorId, it.authorName, it.avatar_url, text, it.timestamp_ms, it.reactions, it.localId, link)
        }
    }

    private fun getLink(content: String): Pair<String, String> {
        val text = HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY)
        var link = ""
        val html = SpannableStringBuilder.valueOf(text).apply {
            getSpans(0, length, URLSpan::class.java).forEach {
                if (it.url.contains(baseUrl)) {
                    link = it.url
                    removeSpan(it)
                }
            }
        }.toHtml()

        return html to link
    }
}