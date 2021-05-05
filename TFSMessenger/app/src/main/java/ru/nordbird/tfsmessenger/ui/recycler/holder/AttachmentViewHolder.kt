package ru.nordbird.tfsmessenger.ui.recycler.holder

import android.view.View
import android.widget.TextView
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.ui.recycler.base.BaseViewHolder
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewHolderClickListener
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped

class AttachmentUi(
    val title: String,
    val url: String,
    override val viewType: Int = R.layout.item_attachment_in
) : ViewTyped {

    override val uid = "$title $url"

    override fun asString() = uid

    companion object {
        fun makeAttachment(title: String, url: String, isIncoming: Boolean): AttachmentUi {
            val viewType = if (isIncoming) R.layout.item_attachment_in else R.layout.item_attachment_out
            return AttachmentUi(title, url, viewType)
        }
    }
}

class AttachmentViewHolder(
    view: View,
    private val clickListener: ViewHolderClickListener
) : BaseViewHolder<AttachmentUi>(view) {

    private val attachmentView: TextView = view.findViewById(R.id.tv_attachment)

    init {
        attachmentView.setOnClickListener { v ->
            clickListener.onViewHolderClick(this, v)
        }
    }

    override fun bind(item: AttachmentUi) {
        attachmentView.text = item.title

        super.bind(item)
    }

}