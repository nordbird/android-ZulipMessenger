package ru.nordbird.tfsmessenger.ui.recycler.holder

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
        messageView.text = HtmlCompat.fromHtml(item.text, HtmlCompat.FROM_HTML_MODE_LEGACY)
        super.bind(item)
    }

}