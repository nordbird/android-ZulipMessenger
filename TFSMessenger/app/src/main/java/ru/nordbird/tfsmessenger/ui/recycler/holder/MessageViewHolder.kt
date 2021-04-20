package ru.nordbird.tfsmessenger.ui.recycler.holder

import android.view.View
import android.widget.ImageView
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.data.model.ReactionGroup
import ru.nordbird.tfsmessenger.extensions.inflate
import ru.nordbird.tfsmessenger.ui.custom.FlexBoxLayout
import ru.nordbird.tfsmessenger.ui.custom.ReactionView
import ru.nordbird.tfsmessenger.ui.recycler.base.BaseViewHolder
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewHolderClickListener
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewHolderClickType
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped

open class MessageUi(
    val id: Int,
    private val authorId: Int,
    val text: String,
    val reactions: List<ReactionGroup>,
    val link: String
) : ViewTyped {

    override val uid = id.toString()

    override fun asString(): String {
        val reactionStr = reactions.map { it.toString() }.reduceOrNull { acc, s -> "$acc $s" }
        return "$authorId $text $reactionStr"
    }
}

open class MessageViewHolder<T : MessageUi>(
    view: View,
    private val reactionResId: Int,
    private val currentUserId: Int,
    private val clickListener: ViewHolderClickListener
) : BaseViewHolder<T>(view) {

    private val flexBox: FlexBoxLayout = view.findViewById(R.id.fbl_reaction)
    private val attachmentView: ImageView = view.findViewById(R.id.iv_attachment)

    init {
        flexBox.btnAddView.setOnClickListener { v ->
            clickListener.onViewHolderClick(this, v, MessageVHClickType.ADD_REACTION_CLICK)
        }
        attachmentView.setOnClickListener { v ->
            clickListener.onViewHolderClick(this, v, MessageVHClickType.GET_ATTACHMENT_CLICK)
        }
    }

    override fun bind(item: T) {
        flexBox.removeAllViews()
        item.reactions.forEach { reaction ->
            val reactionView = flexBox.inflate<ReactionView>(reactionResId)

            reactionView.isSelected = reaction.userIdList.contains(currentUserId)
            reactionView.reactionCount = reaction.userIdList.size
            reactionView.reactionCode = reaction.code
            reactionView.setOnClickListener { v ->
                clickListener.onViewHolderClick(this, v, MessageVHClickType.UPDATE_REACTION_CLICK)
            }
            flexBox.addView(reactionView)
        }
        attachmentView.visibility = if (item.link.isNotBlank()) View.VISIBLE else View.GONE
    }

}

enum class MessageVHClickType : ViewHolderClickType {
    UPDATE_REACTION_CLICK,
    ADD_REACTION_CLICK,
    GET_ATTACHMENT_CLICK
}