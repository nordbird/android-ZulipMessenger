package ru.nordbird.tfsmessenger.data.mapper

import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ImageSpan
import android.text.style.URLSpan
import androidx.core.text.HtmlCompat
import androidx.core.text.toHtml
import ru.nordbird.tfsmessenger.data.model.Message
import ru.nordbird.tfsmessenger.extensions.toZeroTime
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped
import ru.nordbird.tfsmessenger.ui.recycler.holder.MessageInUi
import ru.nordbird.tfsmessenger.ui.recycler.holder.MessageOutUi
import ru.nordbird.tfsmessenger.ui.recycler.holder.SeparatorDateUi
import java.util.*

class MessageToViewTypedMapper(
    private val urlTemplate: String
) : Mapper<List<Message>, List<ViewTyped>> {

    private val reactionMapper = ReactionToReactionGroupMapper()

    override fun transform(data: List<Message>): List<ViewTyped> {
        val messageByDate = data.asReversed().groupBy { Date(it.timestamp_ms).toZeroTime() }
        return messageByDate.flatMap { (date, list) -> makeMessages(list) + SeparatorDateUi(date) }
    }

    private fun makeMessages(messages: List<Message>): List<ViewTyped> {
        return messages.map {
            val (text, link) = getLink(it.content)
            if (it.isIncoming) {
                MessageInUi(it.id.toString(), it.authorId, it.authorName, it.avatar_url, text, reactionMapper.transform(it.reactions), link)
            } else {
                MessageOutUi(it.id.toString(), it.authorId, text, reactionMapper.transform(it.reactions), link)
            }
        }
    }

    private fun getLink(content: String): Pair<String, String> {
        val text = HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY)
        var link = ""
        val html = SpannableStringBuilder.valueOf(text).apply {
            getSpans(0, length, URLSpan::class.java).forEach {
                if (it.url.contains(urlTemplate)) {
                    link = it.url
                    removeSpan(it)
                }
            }
        }.toHtml()

        return html to link
    }
}