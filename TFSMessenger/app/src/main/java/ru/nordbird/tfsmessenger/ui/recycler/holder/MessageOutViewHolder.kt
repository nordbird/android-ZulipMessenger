package ru.nordbird.tfsmessenger.ui.recycler.holder

import android.view.View
import android.widget.TextView
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.data.model.ReactionGroup
import ru.nordbird.tfsmessenger.data.model.User
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewHolderClickListener

class MessageOutUi(
        id: String,
        author: User,
        text: String,
        reactions: List<ReactionGroup>,
        override val viewType: Int = R.layout.item_message_out
) : MessageUi(id, author, text, reactions)

class MessageOutViewHolder(
        view: View,
        currentUser: User?,
        private val clickListener: ViewHolderClickListener,
) : MessageViewHolder<MessageOutUi>(view, R.layout.right_reaction_view, currentUser, clickListener) {

    private val messageView: TextView = view.findViewById(R.id.tv_message)

    init {
        messageView.setOnLongClickListener { v ->
            clickListener.onViewHolderLongClick(this, v)
        }
    }

    override fun bind(item: MessageOutUi) {
        itemId = item.id
        messageView.text = item.text
        super.bind(item)
    }

}