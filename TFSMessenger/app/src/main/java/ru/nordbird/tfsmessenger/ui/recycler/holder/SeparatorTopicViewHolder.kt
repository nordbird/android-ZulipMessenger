package ru.nordbird.tfsmessenger.ui.recycler.holder

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.data.model.TopicColorType
import ru.nordbird.tfsmessenger.ui.recycler.base.BaseViewHolder
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewHolderClickListener
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped

class SeparatorTopicUi(
    val name: String,
    val colorType: TopicColorType,
    override val viewType: Int = R.layout.item_separator_topic
) : ViewTyped {

    override val uid = "SEPARATOR_TOPIC_$name"

    override fun asString() = uid

}

class SeparatorTopicViewHolder(
    view: View,
    private val clickListener: ViewHolderClickListener
) : BaseViewHolder<SeparatorTopicUi>(view) {

    private val nameView: TextView = view.findViewById(R.id.tv_topic_name)

    init {
        nameView.setOnClickListener { v ->
            clickListener.onViewHolderClick(this, v)
        }
    }

    override fun bind(item: SeparatorTopicUi) {
        nameView.text = item.name
        val color = ContextCompat.getColor(nameView.context, item.colorType.color)
        nameView.setBackgroundColor(color)

        super.bind(item)
    }

}
