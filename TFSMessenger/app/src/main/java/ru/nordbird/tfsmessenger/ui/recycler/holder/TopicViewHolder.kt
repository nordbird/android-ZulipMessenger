package ru.nordbird.tfsmessenger.ui.recycler.holder

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import ru.nordbird.tfsmessenger.R
import ru.nordbird.tfsmessenger.data.model.TopicColorType
import ru.nordbird.tfsmessenger.ui.recycler.base.BaseViewHolder
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewHolderClickListener
import ru.nordbird.tfsmessenger.ui.recycler.base.ViewTyped

class TopicUi(
    val name: String,
    val streamName: String,
    val colorType: TopicColorType,
    val messageCount: Int,
    override val viewType: Int = R.layout.item_topic
) : ViewTyped {

    override val uid = "$streamName $name"

    override fun asString() = "$streamName $name $messageCount"

}

class TopicViewHolder(
    view: View,
    private val clickListener: ViewHolderClickListener
) : BaseViewHolder<TopicUi>(view) {

    private val topicBox: LinearLayout = view.findViewById(R.id.ll_topicBox)
    private val nameView: TextView = view.findViewById(R.id.tv_topic_name)
    private val countView: TextView = view.findViewById(R.id.tv_topic_count)

    init {
        topicBox.setOnClickListener { v ->
            clickListener.onViewHolderClick(this, v)
        }
    }

    override fun bind(item: TopicUi) {
        nameView.text = item.name
        val color = ContextCompat.getColor(topicBox.context, item.colorType.color)
        topicBox.setBackgroundColor(color)
        countView.text = countView.resources.getString(R.string.item_topic_count, item.messageCount)

        super.bind(item)
    }

}
