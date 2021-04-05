package ru.nordbird.tfsmessenger.ui.recycler.holder

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.data.model.ReactionGroup
import ru.nordbird.tfsmessenger.data.model.User
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewHolderClickListener

class MessageInUi(
        id: String,
        author: User,
        text: String,
        reactions: List<ReactionGroup>,
        override val viewType: Int = R.layout.item_message_in
) : MessageUi(id, author, text, reactions)

class MessageInViewHolder(
        view: View,
        currentUser: User?,
        private val clickListener: ViewHolderClickListener
) : MessageViewHolder<MessageInUi>(view, R.layout.left_reaction_view, currentUser, clickListener) {

    private val messageBox: LinearLayout = view.findViewById(R.id.ll_messageBox)
    private val authorView: TextView = view.findViewById(R.id.tv_author)
    private val messageView: TextView = view.findViewById(R.id.tv_message)

    init {
        messageBox.setOnLongClickListener { v ->
            clickListener.onViewHolderLongClick(this, v)
        }
    }

    override fun bind(item: MessageInUi) {
        itemId = item.id
        authorView.text = item.author.full_name
        messageView.text = item.text
        super.bind(item)
    }

}
