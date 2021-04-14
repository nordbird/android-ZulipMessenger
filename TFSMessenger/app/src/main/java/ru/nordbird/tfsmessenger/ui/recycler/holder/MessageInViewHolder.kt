package ru.nordbird.tfsmessenger.ui.recycler.holder

import android.graphics.drawable.Drawable
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.data.model.ReactionGroup
import ru.nordbird.tfsmessenger.ui.custom.CircleImageView
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewHolderClickListener

class MessageInUi(
    id: String,
    authorId: Int,
    val authorName: String,
    val avatar: String,
    text: String,
    reactions: List<ReactionGroup>,
    override val viewType: Int = R.layout.item_message_in
) : MessageUi(id, authorId, text, reactions)

class MessageInViewHolder(
    view: View,
    currentUserId: String,
    private val clickListener: ViewHolderClickListener
) : MessageViewHolder<MessageInUi>(view, R.layout.left_reaction_view, currentUserId, clickListener) {

    private val messageBox: LinearLayout = view.findViewById(R.id.ll_messageBox)
    private val authorView: TextView = view.findViewById(R.id.tv_author)
    private val messageView: TextView = view.findViewById(R.id.tv_message)
    private val avatarView: CircleImageView = view.findViewById(R.id.civ_avatar)
    private val target = object : CustomViewTarget<CircleImageView, Drawable>(avatarView) {
        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
            avatarView.avatarDrawable = resource
        }

        override fun onLoadFailed(errorDrawable: Drawable?) {
            avatarView.avatarDrawable = null
        }

        override fun onResourceCleared(placeholder: Drawable?) {
            avatarView.avatarDrawable = null
        }
    }


    init {
        messageBox.setOnLongClickListener { v ->
            clickListener.onViewHolderLongClick(this, v)
        }
    }

    override fun bind(item: MessageInUi) {
        itemId = item.id
        authorView.text = item.authorName
        messageView.text = HtmlCompat.fromHtml(item.text, HtmlCompat.FROM_HTML_MODE_LEGACY)
        messageView.movementMethod = LinkMovementMethod.getInstance()

        if (item.avatar.isBlank()) {
            Glide.with(itemView)
                .clear(target)
        } else {
            Glide.with(itemView)
                .load(item.avatar)
                .into(target)
        }

        super.bind(item)
    }

}
