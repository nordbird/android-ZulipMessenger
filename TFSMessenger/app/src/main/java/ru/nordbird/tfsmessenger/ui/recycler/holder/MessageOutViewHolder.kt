package ru.nordbird.tfsmessenger.ui.recycler.holder

import android.text.util.Linkify
import android.view.View
import android.widget.TextView
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.data.model.Attachment
import ru.nordbird.tfsmessenger.data.model.ReactionGroup
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewHolderClickListener

class MessageOutUi(
    id: Int,
    localId: Int,
    topicName: String,
    authorId: Int,
    text: String,
    reactions: List<ReactionGroup>,
    attachments: List<Attachment>,
    timestamp_ms: Long,
    override val viewType: Int = R.layout.item_message_out
) : MessageUi(id, localId, topicName, authorId, text, reactions, attachments, timestamp_ms)

class MessageOutViewHolder(
    view: View,
    currentUserId: Int,
    private val clickListener: ViewHolderClickListener,
) : MessageViewHolder<MessageOutUi>(view, R.layout.right_reaction_view, currentUserId, clickListener) {

    private val messageView: TextView = view.findViewById(R.id.tv_message)

    init {
        messageView.setOnLongClickListener { v ->
            clickListener.onViewHolderLongClick(this, v)
        }
    }

    override fun bind(item: MessageOutUi) {
        messageView.text = item.text
        Linkify.addLinks(messageView, Linkify.ALL)

        super.bind(item)
    }

}