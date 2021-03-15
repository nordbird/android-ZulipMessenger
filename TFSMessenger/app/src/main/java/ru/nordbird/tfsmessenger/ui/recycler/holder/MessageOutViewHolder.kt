package ru.nordbird.tfsmessenger.ui.recycler.holder

import android.view.View
import android.widget.TextView
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.ui.custom.FlexBoxLayout
import ru.nordbird.tfsmessenger.ui.recycler.base.BaseViewHolder
import ru.nordbird.tfsmessenger.ui.recycler.base.ClickedViewType
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped

class MessageOutUi(
    val text: String,
    override val viewType: Int = R.layout.item_message_out
) : ViewTyped

class MessageOutViewHolder(
    view: View,
    private val clickListener: (ViewTyped, View, ClickedViewType) -> Unit,
    private val longClickListener: (ViewTyped, View, ClickedViewType) -> Unit
) : BaseViewHolder<MessageOutUi>(view) {

    private val messageView: TextView = view.findViewById(R.id.tv_message)
    private val flexBox: FlexBoxLayout = view.findViewById(R.id.fbl_reaction)

    override fun bind(item: MessageOutUi) {
        messageView.text = item.text
        messageView.setOnLongClickListener {
            longClickListener.invoke(item, it, MessageOutClickedViewType.LONG_MESSAGE)
            return@setOnLongClickListener true
        }
        flexBox.btnAddView.setOnClickListener {
            clickListener.invoke(item, it, MessageOutClickedViewType.ADD_REACTION)
        }
        flexBox.getChilds().forEach { child ->
            child.setOnClickListener {
                clickListener.invoke(item, it, MessageOutClickedViewType.REACTION)
            }
        }
    }

}

enum class MessageOutClickedViewType : ClickedViewType {
    LONG_MESSAGE,
    REACTION,
    ADD_REACTION
}