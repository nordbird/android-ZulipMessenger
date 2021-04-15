package ru.nordbird.tfsmessenger.ui.recycler.holder

import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.util.Patterns
import android.view.View
import android.widget.TextView
import androidx.core.text.HtmlCompat
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.data.model.ReactionGroup
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewHolderClickListener

class MessageOutUi(
    id: String,
    authorId: Int,
    text: String,
    reactions: List<ReactionGroup>,
    override val viewType: Int = R.layout.item_message_out
) : MessageUi(id, authorId, text, reactions)

class MessageOutViewHolder(
    view: View,
    currentUserId: String,
    private val clickListener: ViewHolderClickListener,
) : MessageViewHolder<MessageOutUi>(view, R.layout.right_reaction_view, currentUserId, clickListener) {

    private val messageView: TextView = view.findViewById(R.id.tv_message)

    init {
        messageView.setOnLongClickListener { v ->
            clickListener.onViewHolderLongClick(this, v)
        }
    }

    override fun bind(item: MessageOutUi) {
        itemId = item.id
//        messageView.text = item.text
        messageView.text = HtmlCompat.fromHtml(item.text, HtmlCompat.FROM_HTML_MODE_LEGACY)
        messageView.movementMethod = LinkMovementMethod.getInstance()
        messageView.handleUrlClicks {

        }

        super.bind(item)
    }

}

fun TextView.handleUrlClicks(onClicked: ((String) -> Unit)? = null) {
    //create span builder and replaces current text with it
    text = SpannableStringBuilder.valueOf(text).apply {
        //search for all URL spans and replace all spans with our own clickable spans
        getSpans(0, length, URLSpan::class.java).forEach {
            //add new clickable span at the same position
            setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        onClicked?.invoke(it.url)
                    }
                },
                getSpanStart(it),
                getSpanEnd(it),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
            //remove old URLSpan
            removeSpan(it)
        }
    }
    //make sure movement method is set
    movementMethod = LinkMovementMethod.getInstance()
}