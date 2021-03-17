package ru.nordbird.tfsmessenger.ui.recycler.holder

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.data.model.Reaction
import ru.nordbird.tfsmessenger.data.model.User
import ru.nordbird.tfsmessenger.ui.recycler.base.ClickedViewType
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped

class MessageInUi(
    id: String,
    author: User,
    text: String,
    reactions: List<Reaction>,
    override val viewType: Int = R.layout.item_message_in
) : MessageUi(R.layout.left_reaction_view, id, author, text, reactions)

class MessageInViewHolder(
    view: View,
    currentUser: User,
    private val clickListener: (ViewTyped, View, ClickedViewType) -> Unit,
    private val longClickListener: (ViewTyped, View, ClickedViewType) -> Unit
) : MessageViewHolder<MessageInUi>(view, currentUser, clickListener, longClickListener) {

    private val messageBox: LinearLayout = view.findViewById(R.id.ll_messageBox)
    private val authorView: TextView = view.findViewById(R.id.tv_author)
    private val messageView: TextView = view.findViewById(R.id.tv_message)

    override fun bind(item: MessageInUi) {
        authorView.text = item.author.name
        messageView.text = item.text
        messageBox.setOnLongClickListener {
            longClickListener.invoke(item, it, MessageClickedViewType.LONG_MESSAGE)
            return@setOnLongClickListener true
        }

        super.bind(item)
    }

}
