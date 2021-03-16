package ru.nordbird.tfsmessenger.ui.recycler.holder

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.ui.custom.FlexBoxLayout
import ru.nordbird.tfsmessenger.ui.recycler.base.BaseViewHolder
import ru.nordbird.tfsmessenger.ui.recycler.base.ClickedViewType
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped

class MessageInUi(
        val id: String,
        val author: String,
        val text: String,
        override val viewType: Int = R.layout.item_message_in
) : ViewTyped {
    override val uid: String
        get() = id
}

class MessageInViewHolder(
        view: View,
        private val clickListener: (ViewTyped, View, ClickedViewType) -> Unit,
        private val longClickListener: (ViewTyped, View, ClickedViewType) -> Unit
) : BaseViewHolder<MessageInUi>(view) {

    private val messageBox: LinearLayout = view.findViewById(R.id.ll_messageBox)
    private val authorView: TextView = view.findViewById(R.id.tv_author)
    private val messageView: TextView = view.findViewById(R.id.tv_message)
    private val flexBox: FlexBoxLayout = view.findViewById(R.id.fbl_reaction)

    override fun bind(item: MessageInUi) {
        authorView.text = item.author
        messageView.text = item.text
        messageBox.setOnLongClickListener {
            longClickListener.invoke(item, it, MessageInClickedViewType.LONG_MESSAGE)
            return@setOnLongClickListener true
        }
        flexBox.btnAddView.setOnClickListener {
            clickListener.invoke(item, it, MessageInClickedViewType.ADD_REACTION)
        }
        flexBox.getChilds().forEach { child ->
            child.setOnClickListener {
                clickListener.invoke(item, it, MessageInClickedViewType.REACTION)
            }
        }
    }

}

enum class MessageInClickedViewType : ClickedViewType {
    LONG_MESSAGE,
    REACTION,
    ADD_REACTION
}