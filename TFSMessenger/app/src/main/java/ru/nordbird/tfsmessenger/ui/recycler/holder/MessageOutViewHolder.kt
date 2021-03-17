package ru.nordbird.tfsmessenger.ui.recycler.holder

import android.view.View
import android.widget.TextView
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.data.model.Reaction
import ru.nordbird.tfsmessenger.data.model.User
import ru.nordbird.tfsmessenger.ui.recycler.base.ClickedViewType
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped

class MessageOutUi(
    id: String,
    author: User,
    text: String,
    reactions: List<Reaction>,
    override val viewType: Int = R.layout.item_message_out
) : MessageUi(R.layout.right_reaction_view, id, author, text, reactions)

class MessageOutViewHolder(
    view: View,
    currentUser: User,
    private val clickListener: (ViewTyped, View, ClickedViewType) -> Unit,
    private val longClickListener: (ViewTyped, View, ClickedViewType) -> Unit
) : MessageViewHolder<MessageOutUi>(view, currentUser, clickListener, longClickListener) {

    private val messageView: TextView = view.findViewById(R.id.tv_message)

    override fun bind(item: MessageOutUi) {
        messageView.text = item.text
        messageView.setOnLongClickListener {
            longClickListener.invoke(item, it, MessageClickedViewType.LONG_MESSAGE)
            return@setOnLongClickListener true
        }

        super.bind(item)
    }

}