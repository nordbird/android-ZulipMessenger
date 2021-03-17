package ru.nordbird.tfsmessenger.ui.recycler.holder

import android.view.View
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.data.model.Reaction
import ru.nordbird.tfsmessenger.data.model.User
import ru.nordbird.tfsmessenger.extensions.inflate
import ru.nordbird.tfsmessenger.ui.custom.FlexBoxLayout
import ru.nordbird.tfsmessenger.ui.custom.ReactionView
import ru.nordbird.tfsmessenger.ui.recycler.base.BaseViewHolder
import ru.nordbird.tfsmessenger.ui.recycler.base.ClickedViewType
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped

open class MessageUi(
    val reactionRes: Int,
    val id: String,
    val author: User,
    val text: String,
    val reactions: List<Reaction>
) : ViewTyped {

    override val uid: String
        get() = id

    override fun asString(): String {
        val reactionStr = reactions.map { it.toString() }.reduceOrNull { acc, s -> "$acc $s" }
        return "$author $text $reactionStr"
    }
}

open class MessageViewHolder<T : MessageUi>(
    view: View,
    private val currentUser: User,
    private val clickListener: (ViewTyped, View, ClickedViewType) -> Unit,
    private val longClickListener: (ViewTyped, View, ClickedViewType) -> Unit
) : BaseViewHolder<T>(view) {

    private val flexBox: FlexBoxLayout = view.findViewById(R.id.fbl_reaction)

    override fun bind(item: T) {
        flexBox.removeAllViews()
        item.reactions.forEach { reaction ->
            val reactionView = flexBox.inflate<ReactionView>(item.reactionRes)

            reactionView.isSelected = reaction.userIdList.contains(currentUser.id)
            reactionView.reactionCount = reaction.userIdList.size
            reactionView.reactionCode = reaction.code
            reactionView.setOnClickListener { view ->
                clickListener.invoke(item, view, MessageClickedViewType.REACTION)
            }
            flexBox.addView(reactionView)
        }

        flexBox.btnAddView.setOnClickListener { view ->
            clickListener.invoke(item, view, MessageClickedViewType.ADD_REACTION)
        }
    }

}

enum class MessageClickedViewType : ClickedViewType {
    LONG_MESSAGE,
    REACTION,
    ADD_REACTION
}