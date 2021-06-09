package ru.nordbird.tfsmessenger.ui.recycler.holder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.ui.recycler.base.BaseViewHolder
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewHolderClickListener
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewHolderClickType
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped

class StreamUi(
    val id: Int,
    val name: String,
    var topicExpanded: Boolean = false,
    override val viewType: Int = R.layout.item_stream
) : ViewTyped {

    override val uid = id.toString()

    override fun asString() = "$name $topicExpanded"

}

class StreamViewHolder(
    view: View,
    private val clickListener: ViewHolderClickListener
) : BaseViewHolder<StreamUi>(view) {

    private val streamBox: ConstraintLayout = view.findViewById(R.id.cl_streamBox)
    private val streamView: TextView = view.findViewById(R.id.tv_channel)
    private val toggleView: ImageView = view.findViewById(R.id.iv_topic_toggle)

    init {
        streamBox.setOnClickListener { v ->
            clickListener.onViewHolderClick(this, v, StreamVHClickType.OPEN_STREAM_CLICK)
        }
        toggleView.setOnClickListener { v ->
            clickListener.onViewHolderClick(this, v, StreamVHClickType.TOGGLE_TOPICS_CLICK)
        }
    }

    override fun bind(item: StreamUi) {
        streamView.text = item.name
        val imageRes = if (item.topicExpanded) R.drawable.ic_arrow_up_24 else R.drawable.ic_arrow_down_24
        toggleView.setImageResource(imageRes)

        super.bind(item)
    }

}

enum class StreamVHClickType : ViewHolderClickType {
    OPEN_STREAM_CLICK,
    TOGGLE_TOPICS_CLICK
}